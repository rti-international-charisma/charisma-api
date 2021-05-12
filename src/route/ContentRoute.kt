package com.rti.charisma.api.route

import com.rti.charisma.api.service.ContentService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*


@KtorExperimentalLocationsAPI
fun Routing.contentRoute(contentService: ContentService) {

    get("/home") {
        val homePage = contentService.getHomePage()
        call.respond(homePage)
    }

    get("/assessments") {
        val assessment = contentService.getAssessments()
        call.respond(assessment)
    }

    get("/referrals") {
        val referrals = contentService.getReferrals()
        call.respond(referrals)
    }

    get("/modules") {
        val partnerScore = call.parameters["partner_score"]
        val prepConsent = call.parameters["prep_consent"]
        if (partnerScore.isNullOrEmpty() || prepConsent.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing required query parameters")
        } else {
            val modules = contentService.getModules(partnerScore.toInt(), CONSENT.valueOf(prepConsent.toUpperCase()))
            call.respond(modules)
        }
    }

    get("/content/{pageId}") {
        val pageId = call.parameters["pageId"]
        pageId?.let { call.respond(contentService.getPage(pageId)) }
    }

    get("/assets/{assetID}") {
        val asset = contentService.getAsset("${call.parameters["assetID"]}")
        call.respondBytes(asset)
    }

}

enum class CONSENT(val value: String) {
    AGREE("agree"),
    NEUTRAL("neutral"),
    OPPOSE("oppose"),
    UNAWARE("unaware")
}

