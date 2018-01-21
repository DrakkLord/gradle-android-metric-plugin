package net.test.plugin;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Set;

/**
 * Dummy plugin in which checkstyle and pmd can be tested.
 * Created by DrakkLord on 2016. 09. 22..
 */
public class DummyPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
		System.out.println("dummy plugin applied" ) ;
    }
}
