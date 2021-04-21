package service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rti.charisma.api.client.CmsContent
import com.rti.charisma.api.client.ContentClient
import com.rti.charisma.api.exception.ContentException
import com.rti.charisma.api.exception.ContentRequestException
import com.rti.charisma.api.fixtures.AssessmentFixture
import com.rti.charisma.api.fixtures.HomePageFixture
import com.rti.charisma.api.model.ImagesInPage
import com.rti.charisma.api.model.Page
import io.ktor.client.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ContentServiceTest {

    private val contentClient = mockk<ContentClient>(relaxed = true)
    private val contentService = ContentService(contentClient)

    @Test
    fun `it should parse homepage response `() = runBlockingTest {
        val expectedHomePage = HomePageFixture().homePageResult()

        coEvery { contentClient.getClient() } returns mockHttpClient()
        coEvery { contentClient.request("/items/homepage?fields=*.*.*") } returns HomePageFixture().publishedContent()

        val homePage = contentService.getHomePage()

        assertEquals(expectedHomePage, homePage)
    }


    @Test
    fun `it should parse homepage response if content in draft state`() = runBlockingTest {
        val expectedHomePage = HomePageFixture().homePageResult()

        coEvery { contentClient.request("/items/homepage?fields=*.*.*") } returns HomePageFixture().draftContent()

        val homePage = contentService.getHomePage()

        assertEquals(expectedHomePage, homePage)
    }


    @Test
    fun `it should throw exception on error processing response`() = runBlockingTest {
        coEvery { contentClient.request("/items/homepage?fields=*.*.*") } throws (ContentException("Content error"))
        assertFailsWith(
            exceptionClass = ContentException::class,
            block = { contentService.getHomePage() }
        )
    }

    @Test
    fun `it should throw exception on error fetching response`() = runBlockingTest {
        coEvery { contentClient.request("/items/homepage?fields=*.*.*") } throws (ContentRequestException("Content error"))
        assertFailsWith(
            exceptionClass = ContentRequestException::class,
            block = { contentService.getHomePage() }
        )
    }

    @Test
    fun `it should throw content exception if content not in allowed state `() = runBlockingTest {
        coEvery { contentClient.request("/items/homepage?fields=*.*.*") } returns HomePageFixture().archivedContent()
        assertFailsWith(
            exceptionClass = ContentException::class,
            block = { contentService.getHomePage() }
        )
    }

    @Test
    fun `it should parse page response`() = runBlockingTest {
        val expectedPageContent = createPage()

        coEvery { contentClient.request("/items/pages/heartAssessmentIntro?fields=*.*.*") } returns pageContent()

        val pageContent = contentService.getPage("heartAssessmentIntro")

        assertEquals(expectedPageContent, pageContent)
    }


    @Test
    fun `it should throw exception on error processing page response`() = runBlockingTest {
        coEvery { contentClient.request("/items/pages/test-page?fields=*.*.*") } throws (ContentException("Content error"))
        assertFailsWith(
            exceptionClass = ContentException::class,
            block = { contentService.getPage("test-page") }
        )
    }

    @Test
    fun `it should throw exception on error fetching page response`() = runBlockingTest {
        coEvery { contentClient.request("/items/pages/test-page?fields=*.*.*") } throws (ContentRequestException("Content Request Error"))
        assertFailsWith(
            exceptionClass = ContentRequestException::class,
            block = { contentService.getPage("test-page") }
        )
    }

    @Test
    fun `it should throw content exception if page content not in allowed state `() = runBlockingTest {
        coEvery { contentClient.request("/items/pages/test-page?fields=*.*.*") } returns archivedPageContent()
        assertFailsWith(
            exceptionClass = ContentException::class,
            block = { contentService.getPage("test-page") }
        )
    }

    @Test
    fun `it should parse assessment response`() = runBlockingTest {
        val expectedAssessmentContent = AssessmentFixture.assessment()

        coEvery {
            contentClient.request(
                "/items/sections?fields=*," +
                        "questions.questions_id.text,questions.questions_id.options.options_id.*"
            )
        } returns AssessmentFixture.assessmentCmsContent()

        val assessment = contentService.getAssessment()

        assertEquals(expectedAssessmentContent, assessment)
    }

    @Test
    fun `it should throw exception on error processing assessment response`() = runBlockingTest {
        coEvery {
            contentClient.getAssessment(
                "/items/sections?fields=*," +
                        "questions.questions_id.text,questions.questions_id.options.options_id.*"
            )
        } throws (ContentException("Content error"))
        assertFailsWith(
            exceptionClass = ContentException::class,
            block = { contentService.getAssessment() }
        )
    }

    @Test
    fun `it should throw exception on error fetching assessment response`() = runBlockingTest {
        coEvery {
            contentClient.getAssessment(
                "/items/sections?fields=*," +
                        "questions.questions_id.text,questions.questions_id.options.options_id.*"
            )
        } throws (ContentRequestException("Content Request Error"))
        assertFailsWith(
            exceptionClass = ContentRequestException::class,
            block = { contentService.getAssessment() }
        )
    }

    private fun archivedPageContent(): CmsContent {
        val content = """{
            "data": {
		"id": "intro page",
		"title": "This is the landing page",
		"introduction": "This is introduction",
		"summary": "This is description",
		"status": "archived",
        "image_url": "/assets/image-id",
		"images": []
}
}"""
        return jacksonObjectMapper().readValue(content, CmsContent::class.java)
    }

    private fun pageContent(): CmsContent {
        val content = """{
            "data": {
		"id": "intro page",
		"title": "This is the landing page",
		"introduction": "This is introduction",
		"summary": "This is description",
		"status": "published",
        "image_url": "/assets/image-id",
		"images": [
            {
                "id": 4,
                "directus_files_id": {
                    "id": "image1",
                    "title": "Image 1"
                }
            }
        ]
}
}"""
        return jacksonObjectMapper().readValue(content, CmsContent::class.java)
    }

    private fun createPage(): Page {
        return Page(
            "This is the landing page",
            "This is description",
            "This is introduction",
            mutableListOf(ImagesInPage("Image 1", "/assets/image1"))
        )
    }
}