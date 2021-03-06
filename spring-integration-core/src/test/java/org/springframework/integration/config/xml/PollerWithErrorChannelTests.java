/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.config.xml;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;

/**
 * @author Oleg Zhurakousky
 * @author Gunnar Hillert
 *
 */
public class PollerWithErrorChannelTests {

	@Test
	/*
	 * Although adapter configuration specifies header-enricher pointing to the 'eChannel' as errorChannel
	 * the ErrorMessage will still be forwarded to the 'errorChannel' since exception occurs on
	 * receive() and not on send()
	 */
	public void testWithErrorChannelAsHeader() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("PollerWithErrorChannel-context.xml",
				this.getClass());
		SourcePollingChannelAdapter adapter = ac.getBean("withErrorHeader", SourcePollingChannelAdapter.class);

		SubscribableChannel errorChannel = ac.getBean("errorChannel", SubscribableChannel.class);
		MessageHandler handler = mock(MessageHandler.class);
		errorChannel.subscribe(handler);
		adapter.start();
		Thread.sleep(1000);
		verify(handler, atLeastOnce()).handleMessage(Mockito.any(Message.class));
		adapter.stop();
		ac.close();
	}

	@Test
	public void testWithErrorChannel() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("PollerWithErrorChannel-context.xml",
				this.getClass());
		SourcePollingChannelAdapter adapter = ac.getBean("withErrorChannel", SourcePollingChannelAdapter.class);
		adapter.start();
		PollableChannel errorChannel = ac.getBean("eChannel", PollableChannel.class);
		assertNotNull(errorChannel.receive(10000));
		adapter.stop();
		ac.close();
	}

	@Test
	public void testWithErrorChannelAndHeader() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("PollerWithErrorChannel-context.xml",
				this.getClass());
		SourcePollingChannelAdapter adapter = ac.getBean("withErrorChannelAndHeader",
				SourcePollingChannelAdapter.class);
		adapter.start();
		PollableChannel errorChannel = ac.getBean("eChannel", PollableChannel.class);
		assertNotNull(errorChannel.receive(10000));
		adapter.stop();
		ac.close();
	}

	@Test
	// config the same as above but the error wil come from the send
	public void testWithErrorChannelAndHeaderWithSendFailure() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("PollerWithErrorChannel-context.xml",
				this.getClass());
		SourcePollingChannelAdapter adapter = ac.getBean("withErrorChannelAndHeaderErrorOnSend",
				SourcePollingChannelAdapter.class);
		adapter.start();
		PollableChannel errorChannel = ac.getBean("errChannel", PollableChannel.class);
		assertNotNull(errorChannel.receive(10000));
		adapter.stop();
		ac.close();
	}

	@Test
	// INT-1952
	public void testWithErrorChannelAndPollingConsumer() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("PollerWithErrorChannel-context.xml",
				this.getClass());
		MessageChannel serviceWithPollerChannel = ac.getBean("serviceWithPollerChannel", MessageChannel.class);
		QueueChannel errorChannel = ac.getBean("serviceErrorChannel", QueueChannel.class);
		serviceWithPollerChannel.send(new GenericMessage<String>(""));
		assertNotNull(errorChannel.receive(10000));
		ac.close();
	}

	public static class SampleService {

		public String withSuccess() {
			return "hello";
		}

	}

}
