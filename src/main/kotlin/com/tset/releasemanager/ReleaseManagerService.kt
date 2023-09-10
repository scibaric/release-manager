package com.tset.releasemanager

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

interface ReleaseManagerService {
    fun deployService(serviceRequest: ServiceRequest): Int
    fun getServicesBySystemVersionNumber(systemVersionNumber: Int): List<ServiceDto>
}

@Service
class ReleaseManagerServiceImpl : ReleaseManagerService {
    var versionedServices = ConcurrentHashMap<Int, Set<ServiceDto>>()
    var services = ConcurrentHashMap<String, Int>()
    var i = AtomicInteger(0) // TODO: should use kotlin implementation AtomicInt

    override fun deployService(serviceRequest: ServiceRequest): Int {
        when {
            serviceRequest.serviceName.isNullOrBlank() -> throw IllegalArgumentException("Service name should not be null or blank")
            serviceRequest.serviceVersionNumber == null -> throw IllegalArgumentException("Service version number should not be null")
            serviceRequest.serviceVersionNumber < 1 -> throw IllegalArgumentException("Service version number should not be less than 1")
            else -> {
                services[serviceRequest.serviceName] = serviceRequest.serviceVersionNumber

                if (versionedServices.isEmpty()) {
                    val set = ConcurrentHashMap.newKeySet<ServiceDto>()
                    set.add(
                        ServiceDto(
                            serviceName = serviceRequest.serviceName,
                            serviceVersionNumber = serviceRequest.serviceVersionNumber
                        )
                    )
                    versionedServices[i.incrementAndGet()] = set
                    return i.get()
                }

                val tempSet = ConcurrentHashMap.newKeySet<ServiceDto>()
                tempSet.addAll(services.map { ServiceDto(it.key, it.value) })

                if (versionedServices.contains(tempSet)) {
                    return versionedServices.filterValues { it == tempSet }.keys.first()
                }

                versionedServices[i.incrementAndGet()] = tempSet
                return i.get()
            }
        }
    }

    override fun getServicesBySystemVersionNumber(systemVersionNumber: Int): List<ServiceDto> =
        when {
            versionedServices.isEmpty() -> listOf()
            !versionedServices.containsKey(systemVersionNumber) -> listOf()
            else -> versionedServices[systemVersionNumber]!!.map {
                ServiceDto(
                    serviceName = it.serviceName,
                    serviceVersionNumber = it.serviceVersionNumber
                )
            }.toList()
        }
}