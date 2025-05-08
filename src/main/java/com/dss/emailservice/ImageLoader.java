package com.dss.emailservice;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
public class ImageLoader {

    private static final String IMAGE_EXTENSION = "/*.{jpg,png,jpeg}";
    private static final Logger logger = LoggerFactory.getLogger(ImageLoader.class);

    private List<Resource> employeeBirthdayImageResources = new ArrayList<>();
    private List<Resource> managerBirthdayImageResources = new ArrayList<>();
    private List<Resource> workAnniversaryImageResources = new ArrayList<>();
    private Map<String, Resource> iconResourceMap = new HashMap<>();

    // This method loads images from the static folder at application startup
    @PostConstruct
    public void loadResources() {
        loadResourcesForType("employee_birthday_collections", getBirthdayOrAnniversaryFunction(employeeBirthdayImageResources));
        loadResourcesForType("manager_birthday_collections", getBirthdayOrAnniversaryFunction(managerBirthdayImageResources));
        loadResourcesForType("work_anniversary_collections", getBirthdayOrAnniversaryFunction(workAnniversaryImageResources));
        loadResourcesForType("logo", getIconLoaderFunction(iconResourceMap));
    }

    private void loadResourcesForType(String folderName, Consumer<Resource[]> resourceConsumer) {
        String folderPath = "classpath*:/static/" + folderName;
        Resource[] resources = loadResourcesList(folderPath);

        if (resources != null) {
            resourceConsumer.accept(resources);
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

    // Method to get a random birthday template for employees
    public Resource getRandomEmployeeBirthdayTemplate() {
        return getRandomTemplate(employeeBirthdayImageResources);
    }

    // Method to get a random birthday template for managers
    public Resource getRandomManagerBirthdayTemplate() {
        return getRandomTemplate(managerBirthdayImageResources);
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

    public Resource getIconResourceByName(String iconName) {
        return Optional.ofNullable(iconName)
                .map(icon -> iconResourceMap.getOrDefault(icon, null))
                .orElseThrow(() -> new NoSuchElementException(String.format("Unable to fetch resource for icon: %s", iconName)));
    }

    private Consumer<Resource[]> getBirthdayOrAnniversaryFunction(List<Resource> resourceList) {
        return (resources) -> Collections.addAll(resourceList, resources);
    }

    private Consumer<Resource[]> getIconLoaderFunction(Map<String, Resource> resourceMap) {
        return (resources) -> Stream.of(resources)
                .filter(Resource::isReadable)
                .filter(r -> StringUtils.hasText(r.getFilename()))
                .forEach(res -> {
                    String filename = res.getFilename();
                    String key = filename.replaceFirst("[.][^.]+$", ""); // remove extension
                    resourceMap.putIfAbsent(key, res); // only add if key is not already present
                });
    }
}
