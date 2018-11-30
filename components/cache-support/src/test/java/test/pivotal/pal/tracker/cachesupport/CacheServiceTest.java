package test.pivotal.pal.tracker.cachesupport;

import io.pivotal.pal.tracker.cachesupport.CacheConfiguration;
import io.pivotal.pal.tracker.cachesupport.CacheService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CacheConfiguration.class})
public class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RedisTemplate<String, Map<String, Object>> redisTemplate;

    @Before
    public void before() {
        redisTemplate.delete(redisTemplate.keys("*"));
    }

    @Test
    public void testSaveInCache() {
        User user = new User(1L, "leonardo");
        cacheService.save(user.getId(), user, User.class);

        User cachedObject = cacheService.get(user.getId(), User.class);
        Assert.assertEquals(user.getName(), cachedObject.getName());
    }

    @Test
    public void testUpdateCache() {
        User user = new User(1L, "leonardo");
        cacheService.save(user.getId(), user, User.class);

        user.setName("Regis");
        cacheService.save(user.getId(), user, User.class);

        User cachedObjectUpdated = cacheService.get(user.getId(), User.class);
        Assert.assertEquals(user.getName(), cachedObjectUpdated.getName());
    }

    @Test
    public void testSaveMultipleTypes() {
        User user = new User(1L, "leonardo");
        cacheService.save(user.getId(), user, User.class);

        Project project = new Project(1L, "Project A");
        cacheService.save(project.getId(), project, Project.class);

        Assert.assertEquals(2, redisTemplate.keys("*").size());

        User cachedUser = cacheService.get(user.getId(), User.class);
        Project cachedProject = cacheService.get(project.getId(), Project.class);

        Assert.assertEquals(user.getName(), cachedUser.getName());
        Assert.assertEquals(project.getName(), cachedProject.getName());
    }
}
