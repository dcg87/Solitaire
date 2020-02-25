package solitaire;

import java.io.IOException;
import java.util.Scanner;
import java.util.Random;
import java.util.NoSuchElementException;

/**
 * This class implements a simplified version of Bruce Schneier's Solitaire
 * Encryption algorithm.
 * 
 * @author RU NB CS112
 */
public class Solitaire {

	/**
	 * Circular linked list that is the deck of cards for encryption
	 */
	CardNode deckRear;

	/**
	 * Makes a shuffled deck of cards for encryption. The deck is stored in a
	 * circular linked list, whose last node is pointed to by the field deckRear
	 */
	public void makeDeck() {
		// start with an array of 1..28 for easy shuffling
		int[] cardValues = new int[28];
		// assign values from 1 to 28
		for (int i = 0; i < cardValues.length; i++) {
			cardValues[i] = i + 1;
		}

		// shuffle the cards
		Random randgen = new Random();
		for (int i = 0; i < cardValues.length; i++) {
			int other = randgen.nextInt(28);
			int temp = cardValues[i];
			cardValues[i] = cardValues[other];
			cardValues[other] = temp;
		}

		// create a circular linked list from this deck and make deckRear point
		// to its last node
		CardNode cn = new CardNode();
		cn.cardValue = cardValues[0];
		cn.next = cn;
		deckRear = cn;
		for (int i = 1; i < cardValues.length; i++) {
			cn = new CardNode();
			cn.cardValue = cardValues[i];
			cn.next = deckRear.next;
			deckRear.next = cn;
			deckRear = cn;
		}
	}

	/**
	 * Makes a circular linked list deck out of values read from scanner.
	 */
	public void makeDeck(Scanner scanner) throws IOException {
		CardNode cn = null;
		if (scanner.hasNextInt()) {
			cn = new CardNode();
			cn.cardValue = scanner.nextInt();
			cn.next = cn;
			deckRear = cn;
		}
		while (scanner.hasNextInt()) {
			cn = new CardNode();
			cn.cardValue = scanner.nextInt();
			cn.next = deckRear.next;
			deckRear.next = cn;
			deckRear = cn;
		}
	}

	void jokerA() {

		// 1. find joker A
		// 2. swap it with the next node
		CardNode ptr = deckRear.next;
		do {

			if (ptr.cardValue == 27) { // we've found joker A
				swap(ptr, ptr.next); // swap it with the next node
				break; // leave early
			}
			ptr = ptr.next;
		} while (ptr != ptr.next);
	}

	/**
	 * Implements Step 2 - Joker B - on the deck.
	 */
	void jokerB() {
		// 1. find joker B
		// 2. swap it with the next next node
		CardNode ptr = deckRear.next;
		do {

			if (ptr.cardValue == 28) { // we've found joker B
				swap(ptr, ptr.next.next); // swap it with the next next node
				break; // leave early
			}
			ptr = ptr.next;
		} while (ptr != ptr.next);
	}

	/**
	 * Implements Step 3 - Triple Cut - on the deck.
	 */

	private CardNode nextJoker(CardNode start) {

		CardNode ptr = start;
		do {
			if (ptr.cardValue == 27 || ptr.cardValue == 28) {
				return ptr;
			}
			ptr = ptr.next;
		} while (ptr != start);

		return null;
	}

	private CardNode justBeforeNextJoker(CardNode start) {

		CardNode ptr = start;
		do {
			if (ptr.next.cardValue == 27 || ptr.next.cardValue == 28) {
				return ptr;
			}
			ptr = ptr.next;
		} while (ptr != start);

		return null;
	}

	private boolean isJoker(CardNode card) {

		if (card.cardValue == 27 || card.cardValue == 28) {
			return true;
		}
		return false;
	}

	private CardNode justAfterNextJoker(CardNode start) {

		CardNode ptr = start;
		do {
			if (ptr.cardValue == 27 || ptr.cardValue == 28) {
				return ptr.next;
			}
			ptr = ptr.next;
		} while (ptr != start);

		return null;
	}

	/*
	 * 
	 * a_1 -> ... a_km1 -> a_k -> ... a_p -> a_pp1 -> ... -> a_n -> (a_1)
	 */
	void tripleCut() {

		// 1. find ends
		CardNode a_1 = deckRear.next;
		CardNode a_km1 = justBeforeNextJoker(a_1);
		CardNode a_k = nextJoker(a_km1);
		CardNode a_p = nextJoker(a_k);
		CardNode a_pp1 = justAfterNextJoker(a_k);
		CardNode a_n = deckRear;

		// 2. split
		a_n.next = a_pp1;
		a_p.next = a_k;
		a_km1.next = a_1;

		// 3. join
		a_km1.next = a_pp1;
		a_n.next = a_k;
		a_p.next = a_1;

		// 4. rear
		if (isJoker(a_1)) { // if there are no cards before the first joker,
							// then the second joker
							// becomes the last card
			deckRear = a_p;
		} else if (isJoker(a_n)) { // if there are no card after the second
									// joker, then the first joker
									// becomes the first card
			CardNode ptr = a_1;
			do {

				ptr = ptr.next;
			} while (ptr.next != a_k);
			deckRear = ptr;
		} else {
			deckRear = a_km1;
		}

	}

	/**
	 * Implements Step 4 - Count Cut - on the deck.
	 */
	void countCut() {
		// 1. find the value of rear
		int k = deckRear.cardValue;
		if (k == 28) { // special case: should 28 (Joker B) be the last card,
						// treat it as 27
			k = 27;
		}
		// 2. lefty points to front
		CardNode lefty = deckRear.next;
		// 3. righty points to rear
		CardNode righty = deckRear;
		// 4. find the kth node; where to stop swapping
		CardNode kth_node = null;
		CardNode ptr = deckRear;
		do {

			ptr = ptr.next;
			k--;
		} while (k > 0);
		kth_node = ptr;
		// 5. swap lefty and righty until they are the same
		do {

			swap(lefty, righty);
			righty = righty.next;
			if (lefty != kth_node) { // special condition: don't move lefty past
										// the kth node;
										// this allows lefty to catch up
				lefty = lefty.next;
			}
		} while (lefty != righty);
		deckRear = righty;
	}

	/**
	 * Gets a key. Calls the four steps - Joker A, Joker B, Triple Cut, Count
	 * Cut, then counts down based on the value of the first card and extracts
	 * the next card value as key. But if that value is 27 or 28, repeats the
	 * whole process (Joker A through Count Cut) on the latest (current) deck,
	 * until a value less than or equal to 26 is found, which is then returned.
	 * 
	 * @return Key between 1 and 26
	 */
	int getKey() {

		int key_candidate = -1;

		do {

			jokerA(); // works
			jokerB(); // works
			tripleCut();
			countCut();

			// 1. find the value of the first card, k
			int k = deckRear.next.cardValue;
			if (k == 28) { // special condition: if first card is 28, treat it
							// as 27
				k = 27;
			}
			// 2. count k cards from the front (inclusive)
			CardNode ptr = deckRear;
			while (k > 0) {

				ptr = ptr.next;
				k--;
			}
			key_candidate = ptr.next.cardValue;
			// 3. if card not 27 and not 28, then card is key
			// otherwise, repeat process with updated deck
		} while (key_candidate == 27 || key_candidate == 28);

		return key_candidate;
	}

	/**
	 * Utility method that prints a circular linked list, given its rear pointer
	 * 
	 * @param rear
	 *            Rear pointer
	 */
	private static void printList(CardNode rear) {
		if (rear == null) {
			return;
		}
		System.out.print(rear.next.cardValue);
		CardNode ptr = rear.next;
		do {
			ptr = ptr.next;
			System.out.print("," + ptr.cardValue);
		} while (ptr != rear);
		System.out.println("\n");
	}

	/**
	 * Implements Step 1 - Joker A - on the deck.
	 */

	private void swap(CardNode A, CardNode B) {
		int temp = A.cardValue;
		A.cardValue = B.cardValue;
		B.cardValue = temp;
	}

	/*
	 * ASCII 
	 * 65 = a 
	 * 122 = Z
	 * 
	 */
	private String removeNonLetters(String message) {

		if (message.length() > 0) {

			if (message.charAt(0) < 65 || message.charAt(0) > 122) {
				return removeNonLetters(message.substring(1));
			}
			return message.charAt(0) + removeNonLetters(message.substring(1));
		}
		return message;

	}

	private String capitalize(String message) {

		if (message.length() > 0) {

			return Character.toUpperCase(message.charAt(0)) + capitalize(message.substring(1));
		}

		return message;

	}

	/**
	 * Encrypts a message, ignores all characters except upper case letters
	 * 
	 * @param message
	 *            Message to be encrypted
	 * @return Encrypted message, a sequence of upper case letters only
	 */
	public String encrypt(String message) {

		String encrypted_message = ""; 
		message = capitalize(removeNonLetters(message));

		for (int i = 0; i < message.length(); i++) {
			// 1. get a character
			char ch = message.charAt(i);
			// 2. find position in alphabet (convert from letter to number)
			int alpha_pos = ch - 'A' + 1;
			// 3. add key and position
			int encrypted_value = alpha_pos + getKey();
			// 4. if sum greater than 26, subtract 26
			if (encrypted_value > 26) {
				encrypted_value -= 26;
			}
			// 5. convert sum from number to letter
			char encrypted_letter = (char) (encrypted_value - 1 + 'A');
			// 6. append letter to new, encrypted message
			encrypted_message += encrypted_letter;
		}

		return encrypted_message;
	}

	/**
	 * Decrypts a message, which consists of upper case letters only
	 * 
	 * @param message
	 *            Message to be decrypted
	 * @return Decrypted message, a sequence of upper case letters only
	 */
	public String decrypt(String message) {

		String decrypted_message = "";

		for (int i = 0; i < message.length(); i++) {
			// 1. get a key
			int key = getKey();
			// 2. get an encrypted letter
			char ch = message.charAt(i);
			// 3. find alphabetic position of encrypted letter
			int alpha_pos = ch - 'A' + 1;
			if (alpha_pos < key) { 	// special condition: if alphabetic
								  	// position is smaller than key, add 26
				alpha_pos += 26;
			}
			// 4. subtract alphabetic position from key
			int decrypted_value = alpha_pos - key;
			// 5. convert from number to letter
			char decrypted_character = (char) (decrypted_value - 1 + 'A');
			// 6. add letter to new, encrypted message
			decrypted_message += decrypted_character;
		}

		return decrypted_message;
	}
}
