package com.chemscanner.omniscient.services

import com.chemscanner.omniscient.marrow.data.dao.CachedReactionDao
import com.chemscanner.omniscient.marrow.services.GeminiTextService
import com.chemscanner.omniscient.marrow.services.ReactionSimulator
import com.chemscanner.omniscient.marrow.services.SovereignKnowledgeBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ReactionSimulatorTest {

    @Mock
    private lateinit var mockGeminiTextService: GeminiTextService

    @Mock
    private lateinit var mockCachedReactionDao: CachedReactionDao

    @Mock
    private lateinit var mockSovereignKnowledge: SovereignKnowledgeBase

    private lateinit var reactionSimulator: ReactionSimulator

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        reactionSimulator = ReactionSimulator(mockGeminiTextService, mockCachedReactionDao, mockSovereignKnowledge)
    }

    @Test
    fun `simulateReaction with valid reactants returns successful simulation result`() = runTest {
        // Given
        val reactant1 = "H2"
        val reactant2 = "O2"
        val expectedResult = "2H2 + O2 -> 2H2O"
        whenever(mockGeminiTextService.generateContent(any())).thenReturn(expectedResult)
        whenever(mockSovereignKnowledge.getKnowledge(any(), any())).thenReturn(null)
        whenever(mockCachedReactionDao.getReaction(any(), any())).thenReturn(null)

        // When
        val result = reactionSimulator.simulateReaction(reactant1, reactant2)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `simulateReaction with blank reactant1 returns error message`() = runTest {
        // Given
        val reactant1 = ""
        val reactant2 = "O2"

        // When
        val result = reactionSimulator.simulateReaction(reactant1, reactant2)

        // Then
        assertEquals("Provide reactants.", result)
        verify(mockGeminiTextService, never()).generateContent(any())
    }

    @Test
    fun `simulateReaction with blank reactant2 returns error message`() = runTest {
        // Given
        val reactant1 = "H2"
        val reactant2 = " "

        // When
        val result = reactionSimulator.simulateReaction(reactant1, reactant2)

        // Then
        assertEquals("Provide reactants.", result)
        verify(mockGeminiTextService, never()).generateContent(any())
    }

    @Test
    fun `simulateReaction with valid reactants builds correct prompt`() = runTest {
        // Arrange
        val reactant1 = "NaCl"
        val reactant2 = "AgNO3"
        val expectedPrompt = """
            Ești Inteligența Artificială SCI-OS (Ana Istla). 
            Simulează reacția chimică dintre $reactant1 și $reactant2. 
            Oferă ecuația chimică, rezultatul și o explicație scurtă despre utilitatea sa în lumea reală.
        """.trimIndent()
        
        whenever(mockGeminiTextService.generateContent(any())).thenReturn("Some result")
        whenever(mockSovereignKnowledge.getKnowledge(any(), any())).thenReturn(null)
        whenever(mockCachedReactionDao.getReaction(any(), any())).thenReturn(null)

        // Act
        reactionSimulator.simulateReaction(reactant1, reactant2)

        // Assert
        val captor = argumentCaptor<String>()
        verify(mockGeminiTextService).generateContent(captor.capture())
        assertEquals(expectedPrompt, captor.firstValue)
    }

    @Test
    fun `simulateReaction when Gemini fails returns error message`() = runTest {
        // Given
        val reactant1 = "H2"
        val reactant2 = "O2"
        whenever(mockGeminiTextService.generateContent(any())).thenReturn(null)
        whenever(mockSovereignKnowledge.getKnowledge(any(), any())).thenReturn(null)
        whenever(mockCachedReactionDao.getReaction(any(), any())).thenReturn(null)

        // When
        val result = reactionSimulator.simulateReaction(reactant1, reactant2)

        // Then
        assertEquals("Simulation failed. Check Zero-Link connectivity.", result)
    }
}
