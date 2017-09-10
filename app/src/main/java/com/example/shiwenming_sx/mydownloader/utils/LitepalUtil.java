package com.example.shiwenming_sx.mydownloader.utils;

import org.litepal.LitePal;

/**
 * Created by shiwenming_sx on 2017/9/10.
 */

public class LitepalUtil {


	private static LitepalUtil instance;

	private LitepalUtil() {

	}

	public static LitepalUtil getInstance() {

		if (instance == null) {
			instance = new LitepalUtil();
		}
		return instance;
	}


	public  void updataThread() {



	}

	public void updataFile() {


	}


}
