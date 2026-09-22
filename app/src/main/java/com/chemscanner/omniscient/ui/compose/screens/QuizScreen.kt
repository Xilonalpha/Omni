package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemscanner.omniscient.ui.viewmodels.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("KNOWLEDGE TEST", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isQuizFinished) {
                QuizResultView(uiState.score, uiState.totalQuestions, onRestart = { viewModel.restartQuiz() })
            } else {
                val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
                currentQuestion?.let { question ->
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Question ${uiState.currentQuestionIndex + 1} / ${uiState.totalQuestions}", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(question.question, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(Modifier.height(32.dp))
                        
                        question.options.forEachIndexed { index, option ->
                            OptionItem(
                                text = option,
                                isSelected = uiState.selectedOptionIndex == index,
                                isAnswered = uiState.isAnswered,
                                isCorrect = index == question.correctAnswerIndex,
                                onClick = { viewModel.onOptionSelected(index) }
                            )
                        }

                        if (uiState.isAnswered) {
                            Spacer(Modifier.height(24.dp))
                            Text(question.explanation, color = Color.Cyan, fontSize = 14.sp)
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = { viewModel.nextQuestion() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)
                            ) {
                                Text(if (uiState.isLastQuestion) "FINISH" else "NEXT QUESTION", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionItem(text: String, isSelected: Boolean, isAnswered: Boolean, isCorrect: Boolean, onClick: () -> Unit) {
    val borderColor = when {
        isAnswered && isCorrect -> Color.Green
        isAnswered && isSelected && !isCorrect -> Color.Red
        isSelected -> Color.Cyan
        else -> Color.White.copy(0.1f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = !isAnswered) { onClick() },
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, color = Color.White, modifier = Modifier.weight(1f))
            if (isAnswered) {
                if (isCorrect) Icon(Icons.Default.CheckCircle, null, tint = Color.Green)
                else if (isSelected) Icon(Icons.Default.Error, null, tint = Color.Red)
            }
        }
    }
}

@Composable
fun QuizResultView(score: Int, total: Int, onRestart: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("QUIZ COMPLETE", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text("$score / $total", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRestart, colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)) {
            Text("RETRY TEST")
        }
    }
}
