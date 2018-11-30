package io.pivotal.pal.tracker.backlog;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.pivotal.pal.tracker.cachesupport.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RestOperations restOperations;
    private final String endpoint;
    private final CacheService cacheService;

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint, CacheService cacheService) {
        this.restOperations = restOperations;
        this.endpoint = registrationServerEndpoint;
        this.cacheService = cacheService;
    }

    @HystrixCommand(fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo projectInfo = restOperations.getForObject(endpoint + "/projects/" + projectId, ProjectInfo.class);
        cacheService.save(projectId, projectInfo, ProjectInfo.class);
        return projectInfo;
    }

    private ProjectInfo getProjectFromCache(long projectId) {
        logger.info("Getting Project Info from Cache.");
        return cacheService.get(projectId, ProjectInfo.class);
    }
}
