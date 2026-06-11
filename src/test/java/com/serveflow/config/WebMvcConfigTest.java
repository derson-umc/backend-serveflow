package com.serveflow.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WebMvcConfigTest {

    WebMvcConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = new WebMvcConfig();
        Field f = WebMvcConfig.class.getDeclaredField("uploadDir");
        f.setAccessible(true);
        f.set(config, "images");
    }

    @Test
    @DisplayName("registra handler para /images/**")
    void addResourceHandlers_registersImagesHandler() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(eq("/images/**"))).thenReturn(registration);
        when(registry.addResourceHandler(eq("/uploads/images/**"))).thenReturn(registration);
        when(registration.addResourceLocations(any(String.class))).thenReturn(registration);
        when(registration.setCacheControl(any(CacheControl.class))).thenReturn(registration);

        config.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/images/**");
    }

    @Test
    @DisplayName("registra handler para /uploads/images/**")
    void addResourceHandlers_registersUploadsImagesHandler() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(eq("/images/**"))).thenReturn(registration);
        when(registry.addResourceHandler(eq("/uploads/images/**"))).thenReturn(registration);
        when(registration.addResourceLocations(any(String.class))).thenReturn(registration);
        when(registration.setCacheControl(any(CacheControl.class))).thenReturn(registration);

        config.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/images/**");
    }

    @Test
    @DisplayName("configura cache-control nos dois handlers")
    void addResourceHandlers_setsCacheControl() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(any(String.class))).thenReturn(registration);
        when(registration.addResourceLocations(any(String.class))).thenReturn(registration);
        when(registration.setCacheControl(any(CacheControl.class))).thenReturn(registration);

        config.addResourceHandlers(registry);

        verify(registration, times(2)).setCacheControl(any(CacheControl.class));
    }

    @Test
    @DisplayName("configura resource location com path absoluto")
    void addResourceHandlers_usesAbsolutePath() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(any(String.class))).thenReturn(registration);
        when(registration.addResourceLocations(any(String.class))).thenReturn(registration);
        when(registration.setCacheControl(any(CacheControl.class))).thenReturn(registration);

        config.addResourceHandlers(registry);

        verify(registration, times(2)).addResourceLocations(argThat((String loc) ->
                loc.startsWith("file:") && loc.endsWith("/")
        ));
    }
}
