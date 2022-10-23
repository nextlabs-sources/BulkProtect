package com.nextlabs.bulkprotect.task.test;

import java.util.Map;

import com.nextlabs.common.shared.Constants.TokenGroupType;
import com.nextlabs.nxl.RightsManager;

public class Random {
	
	public static void main (String[] args) throws Exception {
		RightsManager rm = new RightsManager("https://trainskydrmrhel74.qapf1.qalab01.nextlabs.com:8443/router", 1, "F1E170AAF75B0FC2A342BC8CD108A491");
		Map<String, String[]> clsProfile = rm.getClassification("afaddd35-4958-426c-b6e2-f52d5148be82", TokenGroupType.TOKENGROUP_SYSTEMBUCKET, null);
		
		for (String key : clsProfile.keySet()) {
			System.out.println(key);
			for (int i=0; i < clsProfile.get(key).length; i++) {
				System.out.println(i + ". " + clsProfile.get(key)[i]);
			}
		}
	}
}
