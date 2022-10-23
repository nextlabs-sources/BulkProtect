package com.nextlabs.bulkprotect.test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeTest {

	public static void main(String[] args) {
		System.out.println(ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusDays(1L).plusMinutes(130L).toEpochSecond());

	}

}
