package com.map.app.service;

/**
 * @author Amit, created on 15-09-2021
 */

public enum PathChoice {
    fastest, /*by travel time*/
    greenest, /*by air pollution exposure*/
    balanced, /*by travel time and air pollution exposure*/
    shortest, /*by distance*/
    all /*all the route types*/
    // see profiles: https://github.com/graphhopper/graphhopper/blob/4.x/docs/core/profiles.md
}
