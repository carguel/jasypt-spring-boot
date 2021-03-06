package com.ulisesbocchio.jasyptspringboot;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.encryption.pbe.config.StringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * <p>Configuration class that registers a {@link BeanFactoryPostProcessor} that wraps all {@link PropertySource} defined in the {@link Environment}
 * with {@link EncryptablePropertySourceWrapper} and defines a default {@link StringEncryptor} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>The {@link StringEncryptor} bean is only defined when no other
 * bean of type {@link StringEncryptor} is present in the Application Context, thus allowing for custom definition if required.</p>
 * <p>The default {@link StringEncryptor} can be configured through the following properties: </p>
 * <table border="1">
 *     <tr>
 *         <td>Key</td><td>Required</td><td>Default Value</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.password</td><td><b>True</b></td><td> - </td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.algorithm</td><td>False</td><td>PBEWithMD5AndDES</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.keyObtentionIterations</td><td>False</td><td>1000</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.poolSize</td><td>False</td><td>1</td>
 *     </tr><tr>
 *         <td>jasypt.encryptor.providerName</td><td>False</td><td>SunJCE</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.saltGeneratorClassname</td><td>False</td><td>org.jasypt.salt.RandomSaltGenerator</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.stringOutputType</td><td>False</td><td>base64</td>
 *     </tr>
 * </table>
 *
 * <p>For mor information about the configuration properties</p>
 * @see StringPBEConfig
 *
 * @author Ulises Bocchio
 */
@Configuration
public class EnableEncryptablePropertySourcesConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EnableEncryptablePropertySourcesConfiguration.class);

    @Bean
    public static EnableEncryptablePropertySourcesPostProcessor enableEncryptablePropertySourcesPostProcessor() {
        return new EnableEncryptablePropertySourcesPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(StringEncryptor.class)
    public StringEncryptor stringEncryptor(Environment environment) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(getRequiredProperty(environment, "jasypt.encryptor.password"));
        config.setAlgorithm(getProperty(environment, "jasypt.encryptor.algorithm", "PBEWithMD5AndDES"));
        config.setKeyObtentionIterations(getProperty(environment, "jasypt.encryptor.keyObtentionIterations", "1000"));
        config.setPoolSize(getProperty(environment, "jasypt.encryptor.poolSize", "1"));
        config.setProviderName(getProperty(environment, "jasypt.encryptor.providerName", "SunJCE"));
        config.setSaltGeneratorClassName(getProperty(environment, "jasypt.encryptor.saltGeneratorClassname", "org.jasypt.salt.RandomSaltGenerator"));
        config.setStringOutputType(getProperty(environment, "jasypt.encryptor.stringOutputType", "base64"));
        encryptor.setConfig(config);
        return encryptor;
    }

    private String getProperty(Environment environment, String key, String defaultValue) {
        if(!propertyExists(environment, key)) {
            LOG.info("Encryptor config not found for property {}, using default value: {}", key, defaultValue);
        }
        return environment.getProperty(key, defaultValue);
    }

    private boolean propertyExists(Environment environment, String key) {
        return environment.getProperty(key) != null;
    }

    private String getRequiredProperty(Environment environment, String key) {
        if(!propertyExists(environment, key)) {
            throw new IllegalStateException(String.format("Required Encryption configuration property missing: %s", key));
        }
        return environment.getProperty(key);
    }
}
