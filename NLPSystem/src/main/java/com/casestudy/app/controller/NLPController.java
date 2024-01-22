
package com.casestudy.app.controller;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class NLPController {

    @GetMapping("/inputPage")
    public String showInputPage() {
        return "inputPage";
    }

    @PostMapping("/processText")
    public String processText(@RequestParam("textInput") String textInput,
                              @RequestParam("fileInput") MultipartFile fileInput,
                              Model model,
                              HttpServletRequest request) {
        String nlpStatistics;

        // Process the text and generate NLP statistics
        Document document;

        if (fileInput != null && !fileInput.isEmpty()) {
            // If a file is uploaded, read its content
            try (BufferedReader br = new BufferedReader(new InputStreamReader(fileInput.getInputStream()))) {
                StringBuilder fileContent = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
                // Create a Stanford NLP Document from the file content
                document = new Document(fileContent.toString());
            } catch (IOException e) {
                // Handle file reading errors
                nlpStatistics = "Error reading the uploaded file.";
                model.addAttribute("nlpStatistics", nlpStatistics);
                return "resultPage";
            }
        } else {
            // If no file is uploaded, create a Stanford NLP Document from the text input
            document = new Document(textInput);
        }

        // Initialize variables
        Map<String, Integer> wordFrequencies = new HashMap<>();
        List<List<String>> namedEntities = document.sentences().stream()
                .map(sentence -> sentence.nerTags())
                .collect(Collectors.toList());
        StringBuilder sentimentResults = new StringBuilder("Sentiment Analysis Results:<br>");
        List<Sentence> sentences = document.sentences();
        Map<String, Long> posDistribution = sentences.stream()
                .flatMap(sentence -> sentence.posTags().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Word Frequencies
        int totalWords = 0;
        for (Sentence sentence : document.sentences()) {
            for (String word : sentence.words()) {
                wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
                totalWords++;
            }
        }

        // Named Entity Recognition Entities
        namedEntities = document.sentences().stream()
                .map(sentence -> sentence.nerTags())
                .collect(Collectors.toList());

        // Sentiment Analysis
        sentimentResults = new StringBuilder("Sentiment Analysis Results:<br>");
        int totalSentiments = 0;
        double sentimentScoreSum = 0;
        for (Sentence sentence : sentences) {
            String sentiment = sentence.sentiment().name();
            sentimentResults.append("Sentence: ").append(sentence).append("<br>");
            sentimentResults.append("Sentiment: ").append(sentiment).append("<br>");
            sentimentScoreSum += getSentimentScore(sentiment);
            totalSentiments++;
        }

        // Part-of-Speech Tagging Distribution
        posDistribution = sentences.stream()
                .flatMap(sentence -> sentence.posTags().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Calculate averages
        double averageWordFrequency = totalWords > 0 ? wordFrequencies.size() / (double) totalWords : 0;
        double averageSentimentScore = totalSentiments > 0 ? sentimentScoreSum / totalSentiments : 0;

        // Add the results to the model for display on the result page
        model.addAttribute("nlpStatistics", createNlpStatisticsString(wordFrequencies, namedEntities, sentimentResults, posDistribution, averageWordFrequency, averageSentimentScore));

        // Add the averages to the session for later retrieval in the comparison page
        request.getSession().setAttribute("averageWordFrequencyInput", averageWordFrequency);
        request.getSession().setAttribute("averageSentimentScoreInput", averageSentimentScore);

        return "resultPage";
    }

    @GetMapping("/compareResultsPage")
    public String compareResultsPage() {
        return "compareResultsPage";
    }

    @PostMapping("/compareResults")
    public String compareResults(@RequestParam("aggregatedResultsFile") MultipartFile aggregatedResultsFile,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 HttpServletRequest request) {
        if (aggregatedResultsFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please upload the aggregated_results file.");
            return "redirect:/inputPage";
        }

        // Process the aggregated results file
        byte[] bytes;
        try {
            bytes = aggregatedResultsFile.getBytes();
            String aggregatedResultsContent = new String(bytes);

            // Retrieve the averages from the session
            Double averageWordFrequencyInput = (Double) request.getSession().getAttribute("averageWordFrequencyInput");
            Double averageSentimentScoreInput = (Double) request.getSession().getAttribute("averageSentimentScoreInput");

            // Calculate averages from the uploaded file
            double averageAggregatedWordFrequency = getAverageAggregatedWordFrequency(aggregatedResultsContent);
            double averageAggregatedSentimentScore = getAverageAggregatedSentimentScore(aggregatedResultsContent);

            // Add the averages to the model for display on the comparison page
            model.addAttribute("averageWordFrequencyInput", averageWordFrequencyInput);
            model.addAttribute("averageSentimentScoreInput", averageSentimentScoreInput);
            model.addAttribute("averageAggregatedWordFrequency", averageAggregatedWordFrequency);
            model.addAttribute("averageAggregatedSentimentScore", averageAggregatedSentimentScore);

            // Print values for troubleshooting
            System.out.println("averageWordFrequencyInput: " + averageWordFrequencyInput);
            System.out.println("averageSentimentScoreInput: " + averageSentimentScoreInput);
            System.out.println("averageAggregatedWordFrequency: " + averageAggregatedWordFrequency);
            System.out.println("averageAggregatedSentimentScore: " + averageAggregatedSentimentScore);

            return "comparisonPage";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error reading the uploaded file.");
            return "redirect:/inputPage";
        }
    }

    private double getAverageAggregatedWordFrequency(String aggregatedResultsContent) {
        // Find the line containing "Average word frequency" and extract the value
        String keyword = "Average word frequency";
        int startIndex = aggregatedResultsContent.indexOf(keyword);
        if (startIndex != -1) {
            startIndex += keyword.length();
            int endIndex = aggregatedResultsContent.indexOf('\n', startIndex);
            if (endIndex != -1) {
                String valueString = aggregatedResultsContent.substring(startIndex, endIndex).replace(",", "").trim();
                try {
                    // Corrected: Parse as Double and return
                    return Double.parseDouble(valueString);
                } catch (NumberFormatException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }
        }
        return 0.0; // Default value if extraction fails
    }

    private double getAverageAggregatedSentimentScore(String aggregatedResultsContent) {
        // Find the line containing "Average sentiment score" and extract the value
        String keyword = "Average sentiment score";
        int startIndex = aggregatedResultsContent.indexOf(keyword);
        if (startIndex != -1) {
            startIndex += keyword.length();
            int endIndex = aggregatedResultsContent.indexOf('\n', startIndex);
            if (endIndex != -1) {
                String valueString = aggregatedResultsContent.substring(startIndex, endIndex).replace(",", "").trim();
                try {
                    // Corrected: Parse as Double and return
                    return Double.parseDouble(valueString);
                } catch (NumberFormatException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }
        }
        return 0.0; // Default value if extraction fails
    }

    private double getSentimentScore(String sentiment) {
        switch (sentiment.toLowerCase()) {
            case "verypositive":
                return 4.0;
            case "positive":
                return 3.0;
            case "neutral":
                return 2.0;
            case "negative":
                return 1.0;
            case "verynegative":
                return 0.0;
            default:
                return 0.0;
        }
    }

    private String createNlpStatisticsString(Map<String, Integer> wordFrequencies, List<List<String>> namedEntities, StringBuilder sentimentResults, Map<String, Long> posDistribution, double averageWordFrequency, double averageSentimentScore) {
        return "Word Frequencies: " + wordFrequencies + "<br>" +
                "Named Entities: " + namedEntities + "<br>" +
                sentimentResults.toString() +
                "POS Tagging Distribution: " + posDistribution + "<br>" +
                "Average Word Frequency: " + averageWordFrequency + "<br>" +
                "Average Sentiment Score: " + averageSentimentScore;
    }
}
