package com.ufrn.imd.websocket.model;

import opennlp.tools.doccat.*;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Chatbot {
    private final Map<String, String> responses;
    private DoccatModel model;

    // Models
    private DocumentCategorizerME categorizer;
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private DictionaryLemmatizer lemmatizer;
    private POSTaggerME posTagger;

    public Chatbot() {
        trainModel();
        loadModels();

        responses = new HashMap<>();

        responses.put("greeting", "Hi there! How can I assist you today?");
        responses.put("understanding", "Climate change refers to long-term changes in temperature, precipitation, and other atmospheric conditions on Earth. It is primarily caused by human activities, such as the burning of fossil fuels and deforestation, which release greenhouse gases into the atmosphere. Global warming is a key aspect of climate change, referring specifically to the increase in Earth's average surface temperature. The greenhouse effect is a natural process that warms the Earth's surface, but human activities have intensified it, leading to accelerated climate change. If you have more questions about climate science, feel free to ask!");
        responses.put("impacts", "Climate change has numerous impacts on the environment, including rising sea levels, more frequent and severe weather events, loss of biodiversity, and disruptions to ecosystems and agriculture. It also has social and economic consequences, such as food and water insecurity, displacement of populations, and increased health risks. Addressing these impacts requires global cooperation and urgent action. Would you like more information on specific impacts?");
        responses.put("solutions", "Addressing climate change requires a combination of mitigation and adaptation strategies. Mitigation efforts focus on reducing greenhouse gas emissions through measures such as transitioning to renewable energy sources, increasing energy efficiency, and implementing policies to limit carbon emissions. Adaptation involves adjusting to the changes that are already occurring, such as building resilient infrastructure, protecting natural habitats, and developing early warning systems for extreme weather events. These solutions require collective action at all levels of society. Do you want to explore any of these solutions further?");
        responses.put("personal", "There are many actions individuals can take to help mitigate climate change. These include reducing energy consumption by using energy-efficient appliances, driving less or using public transportation, eating a plant-based diet, reducing waste, and supporting sustainable businesses and practices. Additionally, individuals can advocate for climate-friendly policies and educate others about the importance of taking action on climate change. Every small action counts towards making a difference! Do you need more ideas for personal climate actions?");
        responses.put("resources", "There are plenty of resources available for learning more about climate change and how to address it. Websites like NASA's Climate Change and Global Warming page, the Intergovernmental Panel on Climate Change (IPCC) website, and the National Oceanic and Atmospheric Administration (NOAA) Climate website offer comprehensive information and data on climate science. You can also find educational materials, reports, and interactive tools from organizations like the World Resources Institute (WRI) and the Union of Concerned Scientists (UCS). These resources can help you stay informed and take action on climate change. Would you like more recommendations for climate change resources?");
        responses.put("completion", "You're welcome! If you have any more questions or need further assistance, feel free to ask.");
    }

    private void trainModel()  {
        var pathToFile = this.getClass().getClassLoader().getResource("train.txt");
        if (pathToFile == null) return;

        try {
            var trainData = new File(Objects.requireNonNull(pathToFile.toURI()));
            var inputStream = new MarkableFileInputStreamFactory(trainData);

            var lineStream = new PlainTextByLineStream(inputStream, StandardCharsets.UTF_8);
            var sampleStream = new DocumentSampleStream(lineStream);

            var factory = new DoccatFactory(new FeatureGenerator[] {new BagOfWordsFeatureGenerator()});
            var trainParameters = ModelUtil.createDefaultTrainingParameters();
            trainParameters.put(TrainingParameters.CUTOFF_PARAM, 0);

            model = DocumentCategorizerME.train("en", sampleStream, trainParameters, factory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InputStream loadInputStream(String filename) {
        return Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(filename));
    }

    private void loadModels() {
        var sentence = loadInputStream("en-sent.bin");
        var tokens = loadInputStream("en-token.bin");
        var lemma = loadInputStream("en-lemmatizer.dict");
        var pos = loadInputStream("en-pos-maxent.bin");

        try {
            tokenizer = new TokenizerME(new TokenizerModel(tokens));
            sentenceDetector = new SentenceDetectorME(new SentenceModel(sentence));
            lemmatizer = new DictionaryLemmatizer(lemma);
            posTagger = new POSTaggerME(new POSModel(pos));
            categorizer = new DocumentCategorizerME(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String answer(String message) {
        StringBuilder response = new StringBuilder();

        if (!message.isEmpty()) {
            var sentences = sentenceDetector.sentDetect(message);
            for (var sentence : sentences) {
                var tokens = tokenizer.tokenize(sentence);
                var partsOfSpeech = posTagger.tag(tokens);

                var lemmas = lemmatizer.lemmatize(tokens, partsOfSpeech);

                var possibleOutcomes = categorizer.categorize(lemmas);
                var category = categorizer.getBestCategory(possibleOutcomes);

                response.append(responses.get(category));
            }
        }
        return response.toString();
    }
}
