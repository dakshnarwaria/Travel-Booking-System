package com.travel.service;

import com.travel.enums.PaymentMode;
import com.travel.exception.PaymentFailedException;
import com.travel.exception.TooManyInvalidAttemptsException;
import com.travel.payment.Payment;
import com.travel.util.Constants;
import com.travel.util.InputHelper;

import java.util.Random;
import java.util.Scanner;

/**
 * Demonstrates: Service-layer orchestration and polymorphic payment strategies.
 */
public class PaymentService {

    private static final int QR_CODE_SIZE = 21;
    private static final int UPI_ID_SUFFIX_RANGE = 10000;
    private static final String QR_BLOCK = "██";
    private static final String QR_EMPTY = "  ";
    private static final int MAX_INVALID_ATTEMPTS = 5;

    public Payment processPayment(Payment payment, double amount, Scanner scanner, String pnr)
            throws PaymentFailedException {
        if (payment == null || scanner == null || pnr == null || pnr.trim().isEmpty()) {
            throw new PaymentFailedException("Payment could not be started because required details are missing.");
        }

        try {
            collectPaymentDetails(payment, amount, scanner, pnr);
            if (!payment.pay(amount)) {
                throw new PaymentFailedException("The payment was declined.");
            }
            return payment;
        } catch (PaymentFailedException e) {
            payment.markFailed();
            throw e;
        }
    }

    private void collectPaymentDetails(Payment payment, double amount, Scanner scanner, String pnr)
            throws PaymentFailedException {
        PaymentMode mode = payment.getPaymentMode();
        switch (mode) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                collectCardDetails(scanner);
                break;
            case UPI:
                collectUpiConfirmation(scanner, pnr);
                break;
            case NET_BANKING:
                readRequired(scanner, "Account number");
                readRequired(scanner, "IFSC code");
                System.out.println("Connecting to bank...");
                break;
            case CASH:
                System.out.printf("Pay at arrival: %s%.0f%n", Constants.CURRENCY, amount);
                if (!readConfirmation(scanner, "Confirm cash payment at arrival?")) {
                    throw new PaymentFailedException("Cash payment was cancelled.");
                }
                break;
            default:
                throw new PaymentFailedException("Unsupported payment mode.");
        }
    }

    private void collectCardDetails(Scanner scanner) throws PaymentFailedException {
        String cardNumber = readRequired(scanner, "Card number");
        readRequired(scanner, "Name on card");
        readRequired(scanner, "Expiry");
        readRequired(scanner, "CVV");
        System.out.println("Card: " + maskCardNumber(cardNumber));
    }

    private void collectUpiConfirmation(Scanner scanner, String pnr) throws PaymentFailedException {
        int suffix = Math.floorMod(pnr.hashCode(), UPI_ID_SUFFIX_RANGE);
        System.out.println("UPI ID: traveler" + suffix + "@travelupi");
        printPseudoQr(pnr);

        int attempts = 0;
        while (true) {
            String input = readLine(scanner, "Press Enter to simulate UPI payment (or type cancel): ");
            if ("cancel".equalsIgnoreCase(input)) {
                throw new PaymentFailedException("UPI payment was cancelled.");
            }
            if (input.isEmpty()) {
                return;
            }
            System.out.println("Press Enter to continue, or type cancel.");
            checkPromptAttempts(++attempts);
        }
    }

    private void printPseudoQr(String pnr) {
        Random random = new Random(pnr.hashCode());
        for (int row = 0; row < QR_CODE_SIZE; row++) {
            StringBuilder line = new StringBuilder(QR_CODE_SIZE * QR_BLOCK.length());
            for (int column = 0; column < QR_CODE_SIZE; column++) {
                line.append(random.nextBoolean() ? QR_BLOCK : QR_EMPTY);
            }
            System.out.println(line);
        }
    }

    private String readRequired(Scanner scanner, String prompt) throws PaymentFailedException {
        int attempts = 0;
        while (true) {
            String input = readLine(scanner, prompt + " (or type cancel): ");
            if ("cancel".equalsIgnoreCase(input)) {
                throw new PaymentFailedException("Payment was cancelled by the user.");
            }
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("This field cannot be empty.");
            checkPromptAttempts(++attempts);
        }
    }

    private boolean readConfirmation(Scanner scanner, String prompt) throws PaymentFailedException {
        int attempts = 0;
        while (true) {
            String input = readLine(scanner, prompt + " (y/n, or type cancel): ");
            if ("cancel".equalsIgnoreCase(input)) {
                throw new PaymentFailedException("Payment was cancelled by the user.");
            }
            if ("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input)) {
                return true;
            }
            if ("n".equalsIgnoreCase(input) || "no".equalsIgnoreCase(input)) {
                return false;
            }
            System.out.println("Please answer y or n.");
            checkPromptAttempts(++attempts);
        }
    }

    private String readLine(Scanner scanner, String prompt) throws PaymentFailedException {
        return InputHelper.readLine(scanner, prompt).trim();
    }

    private void checkPromptAttempts(int attempts) {
        if (attempts >= MAX_INVALID_ATTEMPTS) {
            throw new TooManyInvalidAttemptsException();
        }
    }

    private String maskCardNumber(String cardNumber) {
        int maskedCharacters = Math.max(0, cardNumber.length() - 4);
        StringBuilder masked = new StringBuilder(cardNumber.length());
        for (int i = 0; i < maskedCharacters; i++) {
            masked.append('*');
        }
        masked.append(cardNumber.substring(maskedCharacters));
        return masked.toString();
    }
}
