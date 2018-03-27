package com.ibm.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.config.server.environment.SearchPathLocator
import org.springframework.cloud.config.server.resource.GenericResourceRepository
import org.springframework.cloud.config.server.resource.NoSuchResourceException
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
class MyResourceRepository extends GenericResourceRepository {

    SearchPathLocator myService
    ResourceLoader myResourceLoader

    MyResourceRepository(@Autowired SearchPathLocator service) {
        super(service)
        this.myService = service
    }

    @Override
    void setResourceLoader(ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader)
        this.myResourceLoader = resourceLoader
    }

    synchronized List<Resource> findMultiple(String application, String profile, String label, String[] paths) {
        List<Resource> resources = new ArrayList<>()
        String[] locations = this.myService.getLocations(application, profile, label).getLocations();
        try {
            for (int i = locations.length; i-- > 0;) {
                String location = locations[i];

                for (String path : paths) {
                    Resource file = this.myResourceLoader.getResource(location).createRelative(path);
                    if (file.exists() && file.isReadable()) {
                        resources.push(file);
                    }
                }
            }
        }
        catch (IOException e) {
            // Do nothing as we expect many mismatches
        }
        return resources
    }

    private Collection<String> myGetProfilePaths(String profiles, String path) {
        Set<String> paths = new LinkedHashSet<>();
        for (String profile : StringUtils.commaDelimitedListToSet(profiles)) {
            if (!StringUtils.hasText(profile) || "default".equals(profile)) {
                paths.add(path);
            }
            else {
                String ext = StringUtils.getFilenameExtension(path);
                String file = path;
                if (ext != null) {
                    ext = "." + ext;
                    file = StringUtils.stripFilenameExtension(path);
                }
                else {
                    ext = "";
                }
                paths.add(file + "-" + profile + ext);
            }
        }
        paths.add(path);
        return paths;
    }

}
