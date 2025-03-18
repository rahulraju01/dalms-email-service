package com.dss.emailservice;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class ImageLoader {

    private static final String IMAGE_EXTENSION = "/*.{jpg,png,jpeg}";
    private static final Logger logger = LoggerFactory.getLogger(ImageLoader.class);

    private List<Resource> birthdayImageResources = new ArrayList<>();
    private List<Resource> workAnniversaryImageResources = new ArrayList<>();

    // This method loads images from the static folder at application startup
    @PostConstruct
    public void loadResources() {
        loadResourcesForType("birthday_collections", birthdayImageResources);
        loadResourcesForType("work_anniversary_collections", workAnniversaryImageResources);
    }

    // Generic method to load resources into a list based on folder type
    private void loadResourcesForType(String folderName, List<Resource> resourceList) {
        String folderPath = "classpath*:/static/" + folderName;
        Resource[] resources = loadResourcesList(folderPath);

        if (resources != null) {
            Collections.addAll(resourceList, resources);
        } else {
            logger.error("No resources found for folder: {}", folderName);
        }
    }

    // Helper method to load resources from a given folder
    private Resource[] loadResourcesList(String resourceLoc) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(resourceLoc + IMAGE_EXTENSION);
            logger.info("Total resource found for loc: {} | size: {}", resourceLoc, resources.length);
            return resources;
        } catch (IOException e) {
            logger.error("Error loading resources from location: {}", resourceLoc, e);
            return null;
        }
    }

    // Method to get a random birthday template
    public Resource getRandomBirthdayTemplate() {
        return getRandomTemplate(birthdayImageResources);
    }

    // Method to get a random work anniversary template
    public Resource getRandomWorkAnniversaryTemplate() {
        return getRandomTemplate(workAnniversaryImageResources);
    }

    // Generic method to select a random image from a given list
    private Resource getRandomTemplate(List<Resource> resources) {
        if (resources.isEmpty()) {
            logger.warn("The resource list is empty!");
            return null;
        }
        int randomIndex = new Random().nextInt(resources.size());
        return resources.get(randomIndex);
    }
}
