package com.tset.releasemanager

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/services")
class ReleaseManagerController(private val service: ReleaseManagerService) {

    @PostMapping("/deploy")
    fun deployService(@RequestBody serviceNameRequest: ServiceRequest): Int {
        return service.deployService(serviceNameRequest)
    }

    @GetMapping
    fun getServicesBySystemVersion(@RequestParam systemVersion: Int): List<ServiceDto> {
        return service.getServicesBySystemVersionNumber(systemVersion)
    }
}