package com.pandav.pdvserver.pdvauth;

import com.pandav.pdvserver.pdvauth.Address;
import com.pandav.pdvserver.pdvauth.PdvAuthUtils;
import com.pandav.pdvserver.pdvauth.SigmaProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
public class PdvAuthController {
    private static final String MESSAGE_CONSTANT = "pdvauthexampledapp";

    private final Logger logger = LoggerFactory.getLogger(PdvAuthController.class);

    @GetMapping("/pdvauth/{address}")
    public Object authenticationRequest(@PathVariable String address, HttpServletRequest httpServletRequest) {
        try {
            // we need a SigmaProp for ErgoAuth. Every address is a sigmaprop, so convert
            SigmaProp addressSigmaProp = SigmaProp.createFromAddress(Address.create(address));
            // and we need a message to sign. This message should be unique for our dApp,
            // never occur twice and should not be predictable, so we use a timestring, a unique name
            // and a random component
            String messageToSign = "Authenticate to example dapp\u0000" + new SecureRandom().nextInt(500) + MESSAGE_CONSTANT + System.currentTimeMillis();
            // attention: in production, we must save the message we sent to the user in order to
            // validate the response. This is not done here as this example has no db attached

            PdvAuthRequest request = new PdvAuthRequest();
            request.messageSeverity = PdvAuthRequest.Severity.INFORMATION;
            request.userMessage = "Please sign the message with your address\n" +
                    address +
                    "\nto authenticate to our dApp";
            request.sigmaBoolean = Base64.getEncoder().encodeToString(addressSigmaProp.toBytes());
            request.signingMessage = messageToSign;

            // this example is simplified. Instead, you should give a UUID for the request and
            // save the used sigmaBoolean and signing message to the db here. The UUID should be
            // path variable for the reply to address and used below to fetch the SigmaBoolean
            // and signingMessage data from your db
            request.replyTo = httpServletRequest.getRequestURL().append("/auth").toString();

            return request;
        } catch (Throwable t) {
            PdvAuthRequestError requestError = new PdvAuthRequestError();
            requestError.userMessage = (t.getMessage());
            logger.error("Error round trip", t);
            return requestError;
        }
    }

    @PostMapping("/pdvauth/{address}/auth")
    public String doAuthenticate(@PathVariable String address, @RequestBody PdvAuthResponse authResponse) {
        boolean verified = PdvAuthUtils.verifyResponse(
                SigmaProp.createFromAddress(Address.create(address)), // see statement above
                MESSAGE_CONSTANT, // see statement above
                authResponse.signedMessage,
                Base64.getDecoder().decode(authResponse.proof));

        logger.info("Verification successful: " + verified);

        return "Received";
    }
}