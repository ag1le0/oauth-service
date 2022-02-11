package com.foxconn.fii.main.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RecognizeResponse {

    @JsonProperty("can_door_open")
    private Boolean canDoorOpen;

    private Integer error;

    private PersonRecognize person;

    @Data
    public static class PersonRecognize {
        private Float confidence;

        @JsonProperty("feature_id")
        private Integer featureId;

        private String id;

        private String tag;
    }
}
