/**
 * The TwitterApiHandler class handles all the transfer of information between the LoLProTwitterBot and the Twitter.com Api.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */
package com.hansvg.lolprotwitterbot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

class TwitterApiHandler {

    private final int SECONDS_TO_WAIT_BETWEEN_CALLS = 10;
    private final String ALPHA_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final String oauth_signature_method = "HMAC-SHA1";
    private final String oauth_version = "1.0";
    private String oauth_consumer_key;
    private String oauth_consumer_secret;
    private String oauth_token;
    private String oauth_token_secret;
    private HttpClient httpClient;
    private Logger logger;

    /**
     * TwitterApiHandler class constructor.
     * 
     * @param oauth_consumer_key    Consumer api key for Twitter api
     * @param oauth_consumer_secret Consumer api secret for Twitter api
     * @param oauth_token           Access token for Twitter api
     * @param oauth_token_secret    Access token secret for Twitter api
     * @param logger                The logger object to log what happens in the
     *                              program
     */
    protected TwitterApiHandler(String oauth_consumer_key, String oauth_consumer_secret, String oauth_token,
            String oauth_token_secret, Logger logger) {
        this.httpClient = HttpClient.newHttpClient();
        this.oauth_consumer_key = oauth_consumer_key;
        this.oauth_consumer_secret = oauth_consumer_secret;
        this.oauth_token = oauth_token;
        this.oauth_token_secret = oauth_token_secret;
        this.logger = logger;
    }

    /**
     * Method to post a status update on the twitter bot's twitter account
     * 
     * @param statusToPost The message to be tweeted
     * @return A JSONObject of the tweet returned from the twitter api after a
     *         successful post or null if the tweet was not posted successfully
     */
    protected JSONObject tweet(String statusToPost) {
        try {
            String nonce = generateOauthNonce();
            long timestamp = generateOauthTimestamp();
            String signature = generateSignature(nonce, timestamp, statusToPost);
            String oauthHeader = generateHeaderString(nonce, timestamp, signature);

            HttpRequest request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(""))
                    .uri(new URI(
                            "https://api.twitter.com/1.1/statuses/update.json?status=" + percentEncode(statusToPost)))
                    .header("authorization", oauthHeader).build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // LOG
                this.logger.info("Successfully posted tweet");
                return new JSONObject(response.body());
            } else if (response.statusCode() == 429) {
                // LOG
                this.logger.warning("Twitter Api Rate Limit reached. Retrying after " + (SECONDS_TO_WAIT_BETWEEN_CALLS)
                        + " seconds");
                Thread.sleep(1000 * SECONDS_TO_WAIT_BETWEEN_CALLS);
                return tweet(statusToPost);
            } else {
                // LOG
                this.logger.warning("Error posting tweet to Twitter Api. Status Code: " + response.statusCode());
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            // LOG
            this.logger.severe("NoSuchAlgorithmException");
            return null;
        } catch (InvalidKeyException e) {
            // LOG
            this.logger.severe("InvalidKeyException");
            return null;
        } catch (URISyntaxException e) {
            // LOG
            this.logger.severe("URISyntaxException");
            return null;
        } catch (IOException e) {
            // LOG
            this.logger.severe("IOException");
            return null;
        } catch (InterruptedException e) {
            // LOG
            this.logger.severe("InterruptedException");
            return null;
        }
    }

    /**
     * Method to generate psudo-random nonce for authentication
     * 
     * @return 32 digit psudo-random alphanumaric string
     */
    private String generateOauthNonce() {
        String nonce = "";
        for (int i = 0; i < 32; i++) {
            nonce += ALPHA_CHARACTERS.charAt((int) (Math.random() * 62));
        }
        return nonce;
    }

    /**
     * Method to generate timestamp in form of seconds from epoch for authentication
     * 
     * @return Seconds from epoch as long
     */
    private long generateOauthTimestamp() {
        return (System.currentTimeMillis() / 1000);
    }

    /**
     * Method to percent encode a passed in string
     * 
     * @param nonencodedString
     * @return Percent encoded version of passed in string
     */
    private String percentEncode(String nonencodedString) {
        return URLEncoder.encode(nonencodedString, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Method to generate signature for authentication when posting tweet
     * 
     * @param oauthNonce     The nonce for oauth
     * @param oauthTimestamp The timestamp for oauth
     * @param statusToPost   The message to post
     * @return The signature created returned as a string
     * @throws NoSuchAlgorithmException If the HmacSHA1 algorithm could not be found
     *                                  by the Mac class
     * @throws InvalidKeyException      If the key passed into the init function of
     *                                  the Mac class in invalid
     */
    private String generateSignature(String oauthNonce, long oauthTimestamp, String statusToPost)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String parameterString = "oauth_consumer_key=" + percentEncode(this.oauth_consumer_key) + "&oauth_nonce="
                + percentEncode(oauthNonce) + "&oauth_signature_method=" + percentEncode(this.oauth_signature_method)
                + "&oauth_timestamp=" + percentEncode(Long.toString(oauthTimestamp)) + "&oauth_token="
                + percentEncode(this.oauth_token) + "&oauth_version=" + percentEncode(this.oauth_version) + "&status="
                + percentEncode(statusToPost);

        String signatureBaseString = "POST&" + percentEncode("https://api.twitter.com/1.1/statuses/update.json") + "&"
                + percentEncode(parameterString);

        String signingKey = percentEncode(this.oauth_consumer_secret) + "&" + percentEncode(this.oauth_token_secret);

        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
        mac.init(secret);
        byte[] byteSignature = mac.doFinal(signatureBaseString.getBytes());

        String signature = Base64.getEncoder().encodeToString(byteSignature).trim();

        return signature;
    }

    /**
     * Method that creates the authentication header value for the status post
     * 
     * @param oauthNonce     The nonce for oauth
     * @param oauthTimestamp The timestamp for oauth
     * @param oauthSignature The signature for oauth
     * @return The header string value for authentication
     */
    private String generateHeaderString(String oauthNonce, long oauthTimestamp, String oauthSignature) {
        return "OAuth oauth_consumer_key=\"" + percentEncode(this.oauth_consumer_key) + "\", oauth_nonce=\""
                + percentEncode(oauthNonce) + "\", oauth_signature=\"" + percentEncode(oauthSignature)
                + "\", oauth_signature_method=\"" + percentEncode(this.oauth_signature_method)
                + "\", oauth_timestamp=\"" + percentEncode(Long.toString(oauthTimestamp)) + "\", oauth_token=\""
                + percentEncode(this.oauth_token) + "\", oauth_version=\"" + percentEncode(oauth_version) + "\"";
    }

}