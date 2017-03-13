package com.hb.it.imageserverclient;

import org.junit.Test;

import com.hb.it.imageserverclient.FileServiceUtils;

/**
 * The Class ImageTest.
 */
public class ImageTest {
	
	/**
	 * Test.
	 * @throws Exception 
	 */
	@Test
	public void test() throws Exception {
		String filePath = "/Users/Mx/Downloads/hxjk_01.png";
		String name = FileServiceUtils.uploadFilePathToQCloud("/2.png", filePath);
		System.out.println(name);
	}

}
