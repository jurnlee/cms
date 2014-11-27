package com.webpagebytes.cms.controllers;

import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.cmsdata.WBArticle;
import com.webpagebytes.cms.cmsdata.WBFile;
import com.webpagebytes.cms.cmsdata.WBProject;
import com.webpagebytes.cms.cmsdata.WBUri;
import com.webpagebytes.cms.cmsdata.WBWebPage;
import com.webpagebytes.cms.cmsdata.WBWebPageModule;
import com.webpagebytes.cms.datautility.WPBAdminDataStorage;
import com.webpagebytes.cms.datautility.WPBAdminDataStorageFactory;
import com.webpagebytes.cms.datautility.JSONToFromObjectConverter;
import com.webpagebytes.cms.datautility.WPBAdminDataStorage.AdminSortOperator;
import com.webpagebytes.cms.exception.WPBException;



public class Statistics extends Controller {

	private enum WBEntities
	{
		URIS,
		PAGES,
		MODULES,
		ARTICLES,
		FILES,
		LANGUAGES,
		GLOBALPARAMS		
	};
	private static final String PARAM_ENTITY = "entity"; 
	private static final String PARAM_HISTORY_COUNT = "count"; 
	
	private static final String SORT_PARAM = "lastModified";
	private static final String ERROR_FIELD = "error";
	
	private static final int HISTORY_COUNT = 3;
	private WPBAdminDataStorage adminStorage;
	private JSONToFromObjectConverter jsonObjectConverter;
	
	public Statistics()
	{
		adminStorage = WPBAdminDataStorageFactory.getInstance();
		jsonObjectConverter = new JSONToFromObjectConverter();
	}
	
	private void getRecordsStats(HttpServletRequest request, Class entityClass, org.json.JSONObject payloadJson, String entityName) throws Exception
	{
		org.json.JSONObject returnEntity = new org.json.JSONObject();					
		try
		{
			List<Object> records = adminStorage.getAllRecords(entityClass, SORT_PARAM, AdminSortOperator.DESCENDING);
			Map<String, Object> additionalInfo = new HashMap<String, Object> ();
			List<Object> filteredRecords = filterPagination(request, records, additionalInfo);
			returnEntity.put(DATA, jsonObjectConverter.JSONArrayFromListObjects(filteredRecords));
			returnEntity.put(ADDTIONAL_DATA, additionalInfo);
		} catch (Exception e)
		{
			returnEntity.put(ERROR_FIELD, WPBErrors.WB_CANT_GET_RECORDS);
		}
		payloadJson.put(entityName, returnEntity);		
	}

	private void getLanguagesStats(HttpServletRequest request, org.json.JSONObject payloadJson, String entityName) throws Exception
	{
		org.json.JSONObject returnEntity = new org.json.JSONObject();
		org.json.JSONObject languagesJson = new org.json.JSONObject();					
		try
		{
			WBProject project = adminStorage.get(WBProject.PROJECT_KEY, WBProject.class);
			Set<String> languages = project.getSupportedLanguagesSet();
			languagesJson.put("languages", languages);
			languagesJson.put("defaultLanguage", project.getDefaultLanguage());
			returnEntity.put(DATA, languagesJson);
		} catch (Exception e)
		{
			returnEntity.put(ERROR_FIELD, WPBErrors.WB_CANT_GET_RECORDS);
		}
		payloadJson.put(entityName, returnEntity);		
	}

	
	public void getStatistics(HttpServletRequest request, HttpServletResponse response, String requestUri) throws WPBException
	{
		String [] entities = request.getParameterValues(PARAM_ENTITY);
		org.json.JSONObject payloadJson = new org.json.JSONObject();
		
		try
		{
			if (entities != null)
			{
				for (String entity: entities)
				{
					entity = entity.toUpperCase();
					WBEntities paramEntity = WBEntities.valueOf(entity.toUpperCase());
					switch (paramEntity)
					{
						case URIS:
							getRecordsStats(request, WBUri.class, payloadJson, entity);
							break;
						case PAGES:
							getRecordsStats(request, WBWebPage.class, payloadJson, entity);
							break;
						case MODULES:
							getRecordsStats(request, WBWebPageModule.class, payloadJson, entity);
							break;
						case ARTICLES:
							getRecordsStats(request, WBArticle.class, payloadJson, entity);
							break;
						case FILES:
							getRecordsStats(request, WBFile.class, payloadJson, entity);
							break;
						case LANGUAGES:
							getLanguagesStats(request, payloadJson, entity);
							break;
						case GLOBALPARAMS:
							break;
		
					}
				}
			}
			org.json.JSONObject returnJson = new org.json.JSONObject();
			returnJson.put(DATA, payloadJson);	
			httpServletToolbox.writeBodyResponseAsJson(response, returnJson, null);			

		} catch (Exception e)
		{
			Map<String, String> errors = new HashMap<String, String>();		
			errors.put("", WPBErrors.WB_CANT_GET_RECORDS);
			httpServletToolbox.writeBodyResponseAsJson(response, jsonObjectConverter.JSONObjectFromMap(null), errors);				
		}
	}
}