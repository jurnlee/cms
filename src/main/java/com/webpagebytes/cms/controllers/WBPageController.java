package com.webpagebytes.cms.controllers;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.cache.DefaultWPBCacheFactory;
import com.webpagebytes.cms.cache.WPBCacheFactory;
import com.webpagebytes.cms.cache.WPBWebPagesCache;
import com.webpagebytes.cms.cmsdata.WBFile;
import com.webpagebytes.cms.cmsdata.WBParameter;
import com.webpagebytes.cms.cmsdata.WBResource;
import com.webpagebytes.cms.cmsdata.WBUri;
import com.webpagebytes.cms.cmsdata.WBWebPage;
import com.webpagebytes.cms.datautility.AdminDataStorage;
import com.webpagebytes.cms.datautility.AdminDataStorageFactory;
import com.webpagebytes.cms.datautility.AdminDataStorageListener;
import com.webpagebytes.cms.datautility.WBJSONToFromObjectConverter;
import com.webpagebytes.cms.datautility.AdminDataStorage.AdminQueryOperator;
import com.webpagebytes.cms.datautility.AdminDataStorage.AdminSortOperator;
import com.webpagebytes.cms.datautility.local.WPBLocalAdminDataStorage;
import com.webpagebytes.cms.exception.WBException;
import com.webpagebytes.cms.exception.WBIOException;
import com.webpagebytes.cms.utility.HttpServletToolbox;

public class WBPageController extends WBController implements AdminDataStorageListener<Object>{

	private static final Logger log = Logger.getLogger(WPBLocalAdminDataStorage.class.getName());
	private AdminDataStorage adminStorage;
	private WBPageValidator pageValidator;
	private WPBWebPagesCache wbWebPageCache;
	
	public WBPageController()
	{
		adminStorage = AdminDataStorageFactory.getInstance();
		pageValidator = new WBPageValidator();
		WPBCacheFactory wbCacheFactory = DefaultWPBCacheFactory.getInstance();
		wbWebPageCache = wbCacheFactory.createWBWebPagesCacheInstance(); 
		
		adminStorage.addStorageListener(this);
	}
	
	public void notify (Object t, AdminDataStorageOperation o, Class type)
	{
		try
		{
			if (type.equals(WBWebPage.class))
			{
				log.log(Level.INFO, "WbWebPage datastore notification, going to refresh the cache");
				wbWebPageCache.Refresh();
			}
		} catch (WBIOException e)
		{
			// TBD
		}
	}
	
	public void create(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			String jsonRequest = httpServletToolbox.getBodyText(request);
			WBWebPage webPage = (WBWebPage)jsonObjectConverter.objectFromJSONString(jsonRequest, WBWebPage.class);
			Map<String, String> errors = pageValidator.validateCreate(webPage);
			
			if (errors.size()>0)
			{
				httpServletToolbox.writeBodyResponseAsJson(response, "{}", errors);
				return;
			}
			webPage.setHash( WBWebPage.crc32(webPage.getHtmlSource()));
			webPage.setLastModified(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());
			webPage.setExternalKey(adminStorage.getUniqueId());
			WBWebPage newWebPage = adminStorage.add(webPage);
			
			WBResource resource = new WBResource(newWebPage.getExternalKey(), newWebPage.getName(), WBResource.PAGE_TYPE);
			try
			{
				adminStorage.addWithKey(resource);
			} catch (Exception e)
			{
				// do not propagate further
			}
			org.json.JSONObject returnJson = new org.json.JSONObject();
			returnJson.put(DATA, jsonObjectConverter.JSONFromObject(newWebPage));			
			httpServletToolbox.writeBodyResponseAsJson(response, returnJson, null);

		} catch (Exception e)
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_CREATE_RECORD);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonObjectConverter.JSONObjectFromMap(null), errors);			
		}
	}
	public void getAll(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			Map<String, Object> additionalInfo = new HashMap<String, Object> ();			
			String sortParamDir = request.getParameter(SORT_PARAMETER_DIRECTION);
			String sortParamProp = request.getParameter(SORT_PARAMETER_PROPERTY);
			List<WBWebPage> allRecords = null;
			if (sortParamDir != null && sortParamProp != null)
			{
				if (sortParamDir.equals(SORT_PARAMETER_DIRECTION_ASC))
				{
					additionalInfo.put(SORT_PARAMETER_DIRECTION, SORT_PARAMETER_DIRECTION_ASC);
					additionalInfo.put(SORT_PARAMETER_PROPERTY, sortParamProp);
					allRecords = adminStorage.getAllRecords(WBWebPage.class, sortParamProp, AdminSortOperator.ASCENDING);					
				} else if (sortParamDir.equals(SORT_PARAMETER_DIRECTION_DSC))
				{
					additionalInfo.put(SORT_PARAMETER_DIRECTION, SORT_PARAMETER_DIRECTION_DSC);
					additionalInfo.put(SORT_PARAMETER_PROPERTY, sortParamProp);
					allRecords = adminStorage.getAllRecords(WBWebPage.class, sortParamProp, AdminSortOperator.DESCENDING);
				} else
				{
					allRecords = adminStorage.getAllRecords(WBWebPage.class);					
				}
			} else
			{
				allRecords = adminStorage.getAllRecords(WBWebPage.class);				
			}
					
			List<WBWebPage> result = filterPagination(request, allRecords, additionalInfo);
			
			org.json.JSONObject returnJson = new org.json.JSONObject();
			returnJson.put(DATA, jsonObjectConverter.JSONArrayFromListObjects(result));
			returnJson.put(ADDTIONAL_DATA, jsonObjectConverter.JSONObjectFromMap(additionalInfo));
			httpServletToolbox.writeBodyResponseAsJson(response, returnJson, null);

			
		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_GET_RECORDS);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonObjectConverter.JSONObjectFromMap(null), errors);			
		}
	}
	
	private org.json.JSONObject get(HttpServletRequest request, HttpServletResponse response, WBWebPage webPage) throws WBException
	{
		try
		{
			org.json.JSONObject returnJson = new org.json.JSONObject();
			returnJson.put(DATA, jsonObjectConverter.JSONFromObject(webPage));			
	
			String includeLinks = request.getParameter("include_links");
			if (includeLinks != null && includeLinks.equals("1"))
			{
				List<WBUri> uris = adminStorage.query(WBUri.class, "resourceExternalKey", AdminQueryOperator.EQUAL, webPage.getExternalKey());
				org.json.JSONArray arrayUris = jsonObjectConverter.JSONArrayFromListObjects(uris);
				org.json.JSONObject additionalData = new org.json.JSONObject();
				additionalData.put("uri_links", arrayUris);
				returnJson.put(ADDTIONAL_DATA, additionalData);			
			}
	
			return returnJson;
	
		} catch (Exception e)		
		{
			throw new WBException("cannot get web page details ", e);
		}		
		
	}
	
	public void get(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			Long key = Long.valueOf((String)request.getAttribute("key"));
			WBWebPage webPage = adminStorage.get(key, WBWebPage.class);
			org.json.JSONObject returnJson = get(request, response, webPage);
			httpServletToolbox.writeBodyResponseAsJson(response, returnJson, null);

		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_GET_RECORDS);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonObjectConverter.JSONObjectFromMap(null), errors);			
		}		
	}

	public void getExt(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			String extKey = (String)request.getAttribute("key");
			List<WBWebPage> webPages = adminStorage.query(WBWebPage.class, "externalKey", AdminQueryOperator.EQUAL, extKey);			
			WBWebPage webPage = (webPages.size()>0) ? webPages.get(0) : null; 		
			org.json.JSONObject returnJson = get(request, response, webPage);
			httpServletToolbox.writeBodyResponseAsJson(response, returnJson, null);

		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_GET_RECORDS);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonObjectConverter.JSONObjectFromMap(null), errors);			
		}		
	}

	public void delete(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			Long key = Long.valueOf((String)request.getAttribute("key"));
			WBWebPage tempPage = adminStorage.get(key, WBWebPage.class);
			adminStorage.delete(key, WBWebPage.class);
			
			// delete the owned parameters
			adminStorage.delete(WBParameter.class, "ownerExternalKey", AdminQueryOperator.EQUAL, tempPage.getExternalKey());
			try
			{
				adminStorage.delete(tempPage.getExternalKey(), WBResource.class);
			} catch (Exception e)
			{
				// do not propagate further
			}
			WBWebPage page = new WBWebPage();
			page.setPrivkey(key);
			org.json.JSONObject returnJson = new org.json.JSONObject();
			returnJson.put(DATA, jsonObjectConverter.JSONFromObject(page));			
			httpServletToolbox.writeBodyResponseAsJson(response, returnJson, null);
			
		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_DELETE_RECORD);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonObjectConverter.JSONObjectFromMap(null), errors);			
		}		
	}

	public void update(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WBException
	{
		try
		{
			Long key = Long.valueOf((String)request.getAttribute("key"));
			String jsonRequest = httpServletToolbox.getBodyText(request);
			WBWebPage webPage = (WBWebPage)jsonObjectConverter.objectFromJSONString(jsonRequest, WBWebPage.class);
			webPage.setPrivkey(key);
			Map<String, String> errors = pageValidator.validateUpdate(webPage);
			
			if (errors.size()>0)
			{
				httpServletToolbox.writeBodyResponseAsJson(response, "{}", errors);
				return;
			}
			CRC32 crc = new CRC32();
			crc.update(webPage.getHtmlSource().getBytes());
			webPage.setHash( crc.getValue() );

			webPage.setLastModified(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());
			WBWebPage newWebPage = adminStorage.update(webPage);
			
			WBResource resource = new WBResource(newWebPage.getExternalKey(), newWebPage.getName(), WBResource.PAGE_TYPE);
			try
			{
				adminStorage.update(resource);
			} catch (Exception e)
			{
				// do not propate further
			}
			org.json.JSONObject returnJson = new org.json.JSONObject();
			returnJson.put(DATA, jsonObjectConverter.JSONFromObject(newWebPage));			
			httpServletToolbox.writeBodyResponseAsJson(response, returnJson, null);
	
		} catch (Exception e)		
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WBErrors.WB_CANT_UPDATE_RECORD);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonObjectConverter.JSONObjectFromMap(null), errors);			
		}		
	}
		

	public void setPageValidator(WBPageValidator pageValidator) {
		this.pageValidator = pageValidator;
	}

	public void setHttpServletToolbox(HttpServletToolbox httpServletToolbox) {
		this.httpServletToolbox = httpServletToolbox;
	}

	public void setJsonObjectConverter(
			WBJSONToFromObjectConverter jsonObjectConverter) {
		this.jsonObjectConverter = jsonObjectConverter;
	}

	public void setAdminStorage(AdminDataStorage adminStorage) {
		this.adminStorage = adminStorage;
	}
	public void setPageCache(WPBWebPagesCache pageCache)
	{
		this.wbWebPageCache = pageCache;
	}
	
	
}
