package com.ibm

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class ConfigurableMergeService {
    @Autowired
    private ConfigurableMerge cm;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    void addMapping(String urlPath) throws NoSuchMethodException {

        RequestMappingInfo requestMappingInfo = RequestMappingInfo
                .paths(urlPath)
                .methods(RequestMethod.GET)
                .produces(MediaType.TEXT_PLAIN)
                .build()

        requestMappingHandlerMapping.
                registerMapping(requestMappingInfo, cm,
                        cm.class.getDeclaredMethod(
                                'merge',
                                HttpServletRequest,
                                HttpServletResponse,
                                String,
                                String,
                                String,
                                String,
                                String
                        )
                )
    }
}
