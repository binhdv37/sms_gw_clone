package com.services.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

@Slf4j
@Data
@AllArgsConstructor
public class TemplateCallable implements Callable<Boolean> {

    private String shortMessage;
    private List<String> regexTemplate;

    @Override
    public synchronized Boolean call() {
        boolean matched = false;
        log.debug("Short message: {} in thread: {}", this.shortMessage, Thread.currentThread().getName());
        log.debug("Size of regex template: {} in thread: {}", this.regexTemplate.size(), Thread.currentThread().getName());
        try {
            for (String regex : this.regexTemplate) {
                matched = Pattern.matches(regex, this.shortMessage);
                if (matched) {
                    log.info("Short message: {} matched with regex: {} in thread {}", this.shortMessage, regex, Thread.currentThread().getName());
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("Error at call function in ThreadValidateTemplateImpl with message: {}", e.getMessage());
        }
        return matched;
    }
}
