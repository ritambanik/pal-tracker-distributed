package test.pivotal.pal.tracker.cachesupport;

import java.io.Serializable;
import java.util.Objects;

public class Project implements Serializable {

    private Long id;
    private String name;

    public Project(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
