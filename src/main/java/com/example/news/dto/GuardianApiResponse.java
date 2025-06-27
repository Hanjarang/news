package com.example.news.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuardianApiResponse {
    private Response response;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private String status;
        private int total;
        private int startIndex;
        private int pageSize;
        private int currentPage;
        private int pages;
        private String orderBy;
        private List<GuardianArticle> results;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GuardianArticle {
        private String id;
        private String type;
        private String sectionId;
        private String sectionName;
        private String webPublicationDate;
        private String webTitle;
        private String webUrl;
        private String apiUrl;
        private Fields fields;
        private List<Tag> tags;
        private boolean isHosted;
        private String pillarId;
        private String pillarName;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private String headline;
        private String standfirst;
        private String trailText;
        private String byline;
        private String main;
        private String bodyText;
        private String lastModified;
        private String newspaperEditionDate;
        private String publication;
        private String shortUrl;
        private String thumbnail;
        private String wordcount;
        private String commentCloseDate;
        private String commentable;
        private String isPremoderated;
        private String allowUgc;
        private String isAdvertorial;
        private String showInRelatedContent;
        private String legallySensitive;
        private String lang;
        private String body;
        private String charCount;
        private String shouldHideAdverts;
        private String showAffiliateLinks;
        private String isInappropriateForSponsorship;
        private String isInappropriateForAdverts;
        private String sensitive;
        private String displayHint;
        private String isLive;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tag {
        private String id;
        private String type;
        private String webTitle;
        private String webUrl;
        private String apiUrl;
        private Object references;  // Object로 변경하여 유연하게 처리
        private String bio;
        private String bylineImageUrl;
        private String bylineLargeImageUrl;
        private String firstName;
        private String lastName;
        private String emailAddress;
        private String twitterHandle;
        private String section;
        private String sectionName;
        private String sectionId;
        private String sectionUrl;
        private String sectionWebTitle;
        private String sectionWebUrl;
        private String sectionApiUrl;
        private String sectionEdition;
        private String sectionEditionId;
        private String sectionEditionWebTitle;
        private String sectionEditionWebUrl;
        private String sectionEditionApiUrl;
    }
} 