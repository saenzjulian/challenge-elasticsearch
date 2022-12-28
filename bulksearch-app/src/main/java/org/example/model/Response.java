package org.example.model;

import java.util.List;

public class Response {
    private String target;
    private Long milliseconds;
    private List<String> movies;

    public Response() { }
    public Response(String target, Long time, List<String> movies) {
        this.target = target;
        this.milliseconds = time;
        this.movies = movies;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public List<String> getMovies() {
        return movies;
    }

    public void setMovies(List<String> movies) {
        this.movies = movies;
    }
}
