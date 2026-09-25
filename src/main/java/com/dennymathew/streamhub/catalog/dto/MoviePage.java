package com.dennymathew.streamhub.catalog.dto;
import java.util.List;
public record MoviePage(List<MovieResponse> content, long totalElements, int totalPages, int number, int size) {}
