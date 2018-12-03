package io.pivotal.pal.tracker.allocations;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.client.RestOperations;
import redis.clients.jedis.Jedis;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String registrationServerEndpoint;

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations = restOperations;
        this.registrationServerEndpoint = registrationServerEndpoint;
    }

    @HystrixCommand(fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo projectInfo = restOperations.getForObject(registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class);
        Gson gson = new Gson();
        String json = gson.toJson(projectInfo);
        setKey(String.valueOf(projectId), json);
        return projectInfo;
    }

    private ProjectInfo getProjectFromCache(long projectId) {
        Gson gson = new Gson();
        String json = getKey(String.valueOf(projectId));
        ProjectInfo projectInfo = null;
        if (StringUtils.isNotEmpty(json)) {
           projectInfo = gson.fromJson(json, ProjectInfo.class);
        }
        return projectInfo;
    }

    private RedisInstanceInfo getInfo() {
        LOG.log(Level.WARNING, "Getting Redis Instance Info in Spring controller...");
        // first we need to get the value of VCAP_SERVICES, the environment variable
        // where connection info is stored
        String vcap = System.getenv("VCAP_SERVICES");
        LOG.log(Level.WARNING, "VCAP_SERVICES content: " + vcap);


        // now we parse the json in VCAP_SERVICES
        LOG.log(Level.WARNING, "Using GSON to parse the json...");
        if (StringUtils.isBlank(vcap)) {
            return new RedisInstanceInfo();
        }
        JsonElement root = new JsonParser().parse(vcap);
        JsonObject redis = null;
        if (root != null) {
            if (root.getAsJsonObject().has("p.redis")) {
                redis = root.getAsJsonObject().get("p.redis").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + redis.get("name").getAsString());
            } else if (root.getAsJsonObject().has("p-redis")) {
                redis = root.getAsJsonObject().get("p-redis").getAsJsonArray().get(0).getAsJsonObject();
                LOG.log(Level.WARNING, "instance name: " + redis.get("name").getAsString());
            } else {
                LOG.log(Level.SEVERE, "ERROR: no redis instance found in VCAP_SERVICES");
            }
        }

        // then we pull out the credentials block and produce the output
        if (redis != null) {
            JsonObject creds = redis.get("credentials").getAsJsonObject();
            RedisInstanceInfo info = new RedisInstanceInfo();
            info.setHost(creds.get("host").getAsString());
            info.setPort(creds.get("port").getAsInt());
            info.setPassword(creds.get("password").getAsString());

            // the object will be json serialized automatically by Spring web - we just need to return it
            return info;
        } else return new RedisInstanceInfo();
    }

    private Jedis getJedisConnection() {
        // get our connection info from VCAP_SERVICES
        RedisInstanceInfo info = getInfo();
        Jedis jedis = new Jedis(info.getHost(), info.getPort());

        if (StringUtils.isNotBlank(info.getHost()) && info.getPort() != 0) {
            // make the connection
            jedis.connect();
            // authorize with our password
            jedis.auth(info.getPassword());
            return jedis;
        }
        return null;
    }

    private String setKey(String key, String val) {
        LOG.log(Level.WARNING, "Called the key set method, going to set key: " + key + " to val: " + val);

        if (jedis == null || !jedis.isConnected()) {
            jedis = getJedisConnection();
        }
        if (jedis != null) {
            jedis.set(key, val);
        }

        return "Set key: " + key + " to value: " + val;
    }

    private String getKey(String key) {
        LOG.log(Level.WARNING, "Called the key get method, going to return val for key: " + key);

        if (jedis == null || !jedis.isConnected()) {
            jedis = getJedisConnection();
        }
        if (jedis != null) {
            return jedis.get(key);
        }
        return StringUtils.EMPTY;
    }


    private Logger LOG = Logger.getLogger(ProjectClient.class.getName());

    private Jedis jedis = null;

}
