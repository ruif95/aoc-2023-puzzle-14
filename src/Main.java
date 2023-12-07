import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static final String WRONG_INPUT_FILE_MESSAGE = "Wrong file";
    public static final String INPUT_FILENAME = "input";

    public static void main(String[] args) throws IOException {
        HandFactory handFactory = new HandFactory();

        List<Hand> hands = extractInputLines().stream()
                .map(inputLine -> {
                    String[] handAndRank = inputLine.split(" ");

                    Card[] cards = new Card[5];
                    char[] handCharArray = handAndRank[0].toCharArray();
                    for (int i = 0; i < handCharArray.length; i++) {
                        cards[i] = new Card(handCharArray[i]);
                    }

                    return handFactory.getHand(cards, Integer.parseInt(handAndRank[1]));
                })
                .toList();

        List<Hand> handCopy = new ArrayList<>(hands);
        Collections.sort(handCopy);

        int result = 0;
        for (int i = 0; i < handCopy.size(); i++) {
            result += handCopy.get(i).getRank() * (i+1);
        }
        System.out.println("Result is: " + result);

        // yes this looks horrible but I cannot be bothered to refactor this
    }

    private static List<String> extractInputLines() throws IOException {
        try (InputStream resource = Main.class.getResourceAsStream(INPUT_FILENAME)) {
            if (resource == null) {
                throw new RuntimeException(WRONG_INPUT_FILE_MESSAGE);
            }

            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines()
                    .toList();
        }
    }

    static class HandFactory {
        public Hand getHand(Card[] cards, int rank) {
            Map<Character, Integer> amountPerCharacter = getAmountOfTimesPerCharacter(cards);

            if (isFiveOfAKind(amountPerCharacter)) {
                return new FiveOfAKind(cards, rank);
            } else if (isFourOfAKind(amountPerCharacter)) {
                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(1)) {
                    return new FiveOfAKind(cards, rank);
                }

                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(4)) {
                    return new FiveOfAKind(cards, rank);
                }

                return new FourOfAKind(cards, rank);
            } else if (isFullHouse(amountPerCharacter)) { // AAA JJ OU JJJ AA
                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(2)) {
                    return new FiveOfAKind(cards, rank);
                }

                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(3)) {
                    return new FiveOfAKind(cards, rank);
                }

                return new FullHouse(cards, rank);
            } else if (isThreeOfAKind(amountPerCharacter)) { // JJJ AB    OU       AAA BJ
                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(1)) {
                    return new FourOfAKind(cards, rank);
                }

                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(3)) {
                    return new FourOfAKind(cards, rank);
                }

                return new ThreeOfAKind(cards, rank);
            } else if (isTwoPair(amountPerCharacter)) { // AA BB J    AA JJ B
                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(1)) {
                    return new FullHouse(cards, rank);
                }

                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(2)) {
                    return new FourOfAKind(cards, rank);
                }

                return new TwoPair(cards, rank);
            } else if (isOnePair(amountPerCharacter)) { // AA J BC   OU JJ ABC
                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(1)) {
                    return new ThreeOfAKind(cards, rank);
                }

                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(2)) {
                    return new ThreeOfAKind(cards, rank);
                }

                return new OnePair(cards, rank);
            } else {
                if (amountPerCharacter.containsKey('J') && amountPerCharacter.get('J').equals(1)) {
                    return new OnePair(cards, rank);
                }

                return new Hand(cards, rank);
            }
        }

        private boolean isFiveOfAKind(Map<Character, Integer> amountPerCharacter) {
            return amountPerCharacter.entrySet().stream().anyMatch(entry -> entry.getValue().equals(5));
        }

        private boolean isFourOfAKind(Map<Character, Integer> amountPerCharacter) {
            return amountPerCharacter.entrySet().stream().anyMatch(entry -> entry.getValue().equals(4));
        }

        private boolean isFullHouse(Map<Character, Integer> amountPerCharacter) {
            return amountPerCharacter.entrySet().stream().anyMatch(entry -> entry.getValue().equals(3)) &&
                   amountPerCharacter.entrySet().stream().anyMatch(entry -> entry.getValue().equals(2));

        }

        private boolean isThreeOfAKind(Map<Character, Integer> amountPerCharacter) {
            return amountPerCharacter.entrySet().stream().anyMatch(entry -> entry.getValue().equals(3)) &&
                    amountPerCharacter.entrySet().stream().noneMatch(entry -> entry.getValue().equals(2));
        }

        private boolean isTwoPair(Map<Character, Integer> amountPerCharacter) {
            return amountPerCharacter.entrySet().stream().filter(entry -> entry.getValue().equals(2)).count() == 2;
        }

        private boolean isOnePair(Map<Character, Integer> amountPerCharacter) {
            return amountPerCharacter.entrySet().stream().filter(entry -> entry.getValue().equals(2)).count() ==  1 &&
                    amountPerCharacter.entrySet().stream().filter(entry -> entry.getValue().equals(1)).count() == 3;
        }

        private Map<Character, Integer> getAmountOfTimesPerCharacter(Card[] cards) {
            Map<Character, Integer> nTimesForCharacter = new HashMap<>();

            for (Card card : cards) {
                if (nTimesForCharacter.containsKey(card.getValue())) {
                    nTimesForCharacter.put(card.getValue(), nTimesForCharacter.get(card.getValue()) + 1);
                } else {
                    nTimesForCharacter.put(card.getValue(), 1);
                }
            }

            return nTimesForCharacter;
        }
    }

    static class Card implements Comparable<Card> {
        static Map<Character, Integer> VALUE_TO_INT = new HashMap<>() {{
                put('A', 14);
                put('K', 13);
                put('Q', 12);
                put('J', 1);
                put('T', 10);
                put('9', 9);
                put('8', 8);
                put('7', 7);
                put('6', 6);
                put('5', 5);
                put('4', 4);
                put('3', 3);
                put('2', 2);
        }};
        char value;

        private final int intValue;

        public Card(char value) {
            this.value = value;
            this.intValue = VALUE_TO_INT.get(value);
        }

        char getValue() {
            return value;
        }

        int getIntValue() {
            return intValue;
        }

        @Override
        public int compareTo(Card otherCard) {
            return Integer.compare(getIntValue(), otherCard.getIntValue());
        }
    }
    static class Hand implements Comparable<Hand> {
        Card[] cards;
        int rank;
        int handComparationValue = 0;

        public Hand(Card[] cards, int rank) {
            this.cards = cards;
            this.rank = rank;
        }

        int getHandComparationValue() {
            return handComparationValue;
        }

        int getRank() {
            return this.rank;
        }

        Card[] getCards() {
            return this.cards;
        }

        @Override
        public int compareTo(Hand o) {
            if (getHandComparationValue() != o.getHandComparationValue()) {
                return Integer.compare(getHandComparationValue(), o.getHandComparationValue());
            }

            for (int i = 0; i < cards.length; i++) {
                int cardComparisonValue = cards[i].compareTo(o.getCards()[i]);

                if (cardComparisonValue == 0) {
                    continue;
                }

                return cardComparisonValue;
            }

            return 0;
        }
    }

    static class FiveOfAKind extends Hand {
        public FiveOfAKind(Card[] cards, int rank) {
            super(cards, rank);
            this.handComparationValue = 6;
        }
    }

    static class FourOfAKind extends Hand {
        public FourOfAKind(Card[] cards, int rank) {
            super(cards, rank);
            this.handComparationValue = 5;
        }
    }

    static class FullHouse extends Hand {
        public FullHouse(Card[] cards, int rank) {
            super(cards, rank);
            this.handComparationValue = 4;
        }
    }

    static class ThreeOfAKind extends Hand {
        public ThreeOfAKind(Card[] cards, int rank) {
            super(cards, rank);
            this.handComparationValue = 3;
        }
    }

    static class TwoPair extends Hand {
        public TwoPair(Card[] cards, int rank) {
            super(cards, rank);
            this.handComparationValue = 2;
        }
    }

    static class OnePair extends Hand {
        public OnePair(Card[] cards, int rank) {
            super(cards, rank);
            this.handComparationValue = 1;
        }
    }
}
