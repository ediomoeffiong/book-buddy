package com.bookbuddy.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksResponse {
    
    private String kind;
    private Integer totalItems;
    private List<GoogleBookItem> items;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleBookItem {
        private String id;
        private VolumeInfo volumeInfo;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeInfo {
        private String title;
        private List<String> authors;
        private String publisher;
        private String publishedDate;
        private String description;
        private List<IndustryIdentifier> industryIdentifiers;
        private Integer pageCount;
        private List<String> categories;
        private Double averageRating;
        private Integer ratingsCount;
        private ImageLinks imageLinks;
        private String language;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndustryIdentifier {
        private String type;
        private String identifier;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageLinks {
        private String smallThumbnail;
        private String thumbnail;
        private String small;
        private String medium;
        private String large;
    }
}

