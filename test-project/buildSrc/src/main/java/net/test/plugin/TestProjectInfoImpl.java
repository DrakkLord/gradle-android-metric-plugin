package com.drakklord.gradle.metric.core.model;

/**
 * Created by DrakkLord on 2016. 10. 24..
 */
public class TestProjectInfoImpl implements TestProjectInfo {

	@Override
    public String getDataFromGradle() {
		return "this is a line from gradle";
	}
}
