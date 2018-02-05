package com.ibm.controller

import com.ibm.service.ConfigService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class Merge extends AbstractMerge{

    @Autowired
    ConfigService configService

    @ResponseBody
    @RequestMapping(value = "/merge/{name}/{profile}.{format}", method = RequestMethod.GET)
    void merge(
            @PathVariable('name') String name,
            @PathVariable('profile') String profile,
            @PathVariable('format') String format,
//            @RequestParam(value='tag', required = false) String tag,
//            @RequestParam(value='branch', required = false) String branch,
            @RequestParam(value='label', required = false) String label,
            HttpServletRequest request,
            HttpServletResponse response) {
        response.getOutputStream().println(merge(request,name,Arrays.asList(profile.split(",")),label,format))
    }

}
