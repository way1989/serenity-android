/**
 * The MIT License (MIT)
 * Copyright (c) 2012 David Carver
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package us.nineworlds.serenity.core.services.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import us.nineworlds.serenity.NanoHTTPD;
import us.nineworlds.serenity.core.model.impl.MenuItem;


/**
 * @author dcarver
 *
 */
@RunWith(RobolectricTestRunner.class)
public class MainMenuIntentServiceTest {
	
	NanoHTTPD server = null;
	MockMainMenuIntentService menuService;
	
	@Before
	public void setUp() throws Exception {
		
		URL url = this.getClass().getResource("/");
		File rootfile = new File(url.getPath());
		server = new NanoHTTPD(32400, rootfile);
		menuService = new MockMainMenuIntentService();
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void onHandleIntent() throws Exception {
		menuService.onHandleIntent(null);
		ArrayList<MenuItem> menuItems = menuService.getMenuItems();
		assertNotNull(menuItems);
		assertTrue(menuItems.size() > 0); 
	}
	
	@Test
	public void testHasSettingsType() throws Exception {
		menuService.onHandleIntent(null);
		
		for (MenuItem item : menuService.getMenuItems()) {
			if ("settings".equals(item.getType())) {
				return;
			}
		}
	}
	
	@Test
	public void testHasOptionsMenuItem() throws Exception {
		menuService.onHandleIntent(null);
		
		for (MenuItem item : menuService.getMenuItems()) {
			if ("Options".equals(item.getTitle())) {
				return;
			}
		}
	}
	
	
	@Test
	public void testHasMovieType() throws Exception {
		menuService.onHandleIntent(null);
		
		for (MenuItem item : menuService.getMenuItems()) {
			if ("movie".equals(item.getType())) {
				return;
			}
		}
		fail("No movies were found.");
	}

}
