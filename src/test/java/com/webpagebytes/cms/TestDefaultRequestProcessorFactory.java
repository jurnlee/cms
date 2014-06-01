package com.webpagebytes.cms;
import com.webpagebytes.cms.AjaxRequestProcessor;

import com.webpagebytes.cms.BaseRequestProcessorFactory;
import com.webpagebytes.cms.ResourceRequestProcessor;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class TestDefaultRequestProcessorFactory {

	@Test
	public void testCreateResourceRequestProcessor()
	{
		BaseRequestProcessorFactory processorFactory = new BaseRequestProcessorFactory();
		ResourceRequestProcessor processor = processorFactory.createResourceRequestProcessor();
		assertTrue (processor != null);
	}

	@Test
	public void testCreateAjaxRequestProcessor()
	{
		BaseRequestProcessorFactory processorFactory = new BaseRequestProcessorFactory();
		AjaxRequestProcessor processor = processorFactory.createAjaxRequestProcessor();
		assertTrue (processor != null);
	}

}
