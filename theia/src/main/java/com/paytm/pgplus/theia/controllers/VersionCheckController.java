package com.paytm.pgplus.theia.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author karankapoor
 */

@RestController
public class VersionCheckController {
    private static final String CATALINA_BASE = System.getProperty("catalina.base");
    private static final String FILE_LOCATION = CATALINA_BASE.concat("/conf/versioncheck.json");

    @ResponseBody
    @RequestMapping(value = "/versioncheck", method = { RequestMethod.GET })
    public ResponseEntity<Object> checkVersion() {

        File file = new File(FILE_LOCATION);
        if (file.exists() && !file.isDirectory()) {
            String version;
            try {
                String fileContent = new String(Files.readAllBytes(Paths.get(FILE_LOCATION)));
                return new ResponseEntity<Object>(fileContent, HttpStatus.OK);
            } catch (IOException e) {
                return new ResponseEntity<Object>("File could not be read", HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity<Object>("File not found", HttpStatus.NOT_FOUND);
    }
}
