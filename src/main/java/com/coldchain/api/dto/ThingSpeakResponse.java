package com.coldchain.api.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ThingSpeakResponse {

    private List<Feed> feeds;

    @Getter
    @Setter
    public static class Feed {

        private String created_at;

        private String field1;
        private String field2;
        private String field3;
        private String field4;
        private String field5;
        private String field6;
        private String field7;
        private String field8;
    }
}