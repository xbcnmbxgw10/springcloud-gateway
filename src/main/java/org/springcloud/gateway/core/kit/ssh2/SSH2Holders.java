/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springcloud.gateway.core.kit.ssh2;

import org.springcloud.gateway.core.core.ObjectInstantiators;
import org.springcloud.gateway.core.function.CallbackFunction;
import org.springcloud.gateway.core.function.ProcessFunction;
import org.springcloud.gateway.core.log.SmartLogger;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springcloud.gateway.core.lang.Assert2.isTrue;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;

/**
 * {@link SSH2Holders}, generic SSH2 client wrapper tool. </br>
 * Including the implementation of ethz/ssj/ssd.
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @see
 */
public abstract class SSH2Holders<S, F> {

    private static final SmartLogger log = getLogger(SSH2Holders.class);

    /**
     * Gets default {@link SSH2Holders} instance by provider class.
     * 
     * @param <T>
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final static <T extends SSH2Holders> T getDefault() {
        // @see:dopass-infras-common/pom.xml#<groupId>com.hierynomus</groupId>
        return (T) getInstance(SshjHolder.class);
    }

    /**
     * Gets {@link SSH2Holders} instance by provider class.
     * 
     * @param <T>
     * @param providerClass
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final static <T extends SSH2Holders> T getInstance(Class<T> providerClass) {
        T t = (T) registry.get(providerClass);
        if (isNull(t)) {
            synchronized (SSH2Holders.class) {
                t = (T) registry.get(providerClass);
                if (isNull(t)) {
                    registry.put(providerClass, (t = ObjectInstantiators.newInstance(providerClass)));
                }
            }
        }
        return t;
    }

    // --- Transfer files. ---

    /**
     * Transfer get file from remote host.(user sftp)
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param localFile
     * @param remoteFilePath
     * @throws Exception
     */
    public abstract void scpGetFile(String host, String user, char[] pemPrivateKey, String password, File localFile,
            String remoteFilePath) throws Exception;

    /**
     * Transfer put file to remote host directory.
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param localFile
     * @param remoteDir
     * @throws Exception
     */
    public abstract void scpPutFile(String host, String user, char[] pemPrivateKey, String password, File localFile,
            String remoteDir) throws Exception;

    /**
     * Perform file transfer with remote host, including scp.put/upload or
     * scp.get/download.
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param processor
     * @throws IOException
     */
    protected abstract void doScpTransfer(String host, String user, char[] pemPrivateKey, String password,
            CallbackFunction<F> processor) throws Exception;

    // --- Execution commands. ---

    /**
     * Execution commands with SSH2.
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public abstract Ssh2ExecResult execWaitForResponse(String host, String user, char[] pemPrivateKey, String password,
            String command, long timeoutMs) throws Exception;

    /**
     * Execution commands wait for complete with SSH2
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param processor
     * @param timeoutMs
     * @return
     * @throws IOException
     */
    public abstract <T> T execWaitForComplete(String host, String user, char[] pemPrivateKey, String password, String command,
            ProcessFunction<S, T> processor, long timeoutMs) throws Exception;

    /**
     * Execution commands with SSH2
     * 
     * @param host
     * @param user
     * @param pemPrivateKey
     * @param command
     * @param processor
     * @return
     * @throws IOException
     */
    public abstract <T> T doExecCommand(String host, String user, char[] pemPrivateKey, String password, String command,
            ProcessFunction<S, T> processor) throws Exception;

    /**
     * Get local current user ssh authentication private key of default.
     * 
     * @param host
     * @param user
     * @return
     * @throws Exception
     */
    protected final char[] getDefaultLocalUserPrivateKey() throws Exception {
        // Check private key.
        File privateKeyFile = new File(USER_HOME + "/.ssh/id_rsa");
        isTrue(privateKeyFile.exists(), String.format("Not found privateKey for %s", privateKeyFile));

        log.warn("Fallback use local user pemPrivateKey of: {}", privateKeyFile);
        try (CharArrayWriter cw = new CharArrayWriter(); FileReader fr = new FileReader(privateKeyFile.getAbsolutePath())) {
            char[] buff = new char[256];
            int len = 0;
            while ((len = fr.read(buff)) != -1) {
                cw.write(buff, 0, len);
            }
            return cw.toCharArray();
        }
    }

    // --- Tool function's. ---

    /**
     * Generate keypair of SSH2 based on RSA/DSA/ECDSA.
     * 
     * @param type
     *            Algorithm type(RSA/DSA/ECDSA).
     * @param comment
     * @return
     * @throws Exception
     */
    public abstract Ssh2KeyPair generateKeypair(AlgorithmType type, String comment) throws Exception;

    /**
     * {@link SSH2Holders} providers registry.
     */
    @SuppressWarnings("rawtypes")
    private final static Map<Class<? extends SSH2Holders>, SSH2Holders> registry = new HashMap<>();

    /**
     * Default IO buffer size.
     */
    final public static int DEFAULT_TRANSFER_BUFFER = 1024 * 6;

    /**
     * {@link Ssh2ExecResult}
     * 
     * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
     * @version v1.0.0
     * @see
     */
    public final static class Ssh2ExecResult {

        /** Remote commands exit signal. */
        final private String exitSignal;

        /** Remote commands exit code. */
        final private Integer exitCode;

        /** Standard message */
        final private String message;

        /** Error message */
        final private String errmsg;

        public Ssh2ExecResult(String exitSignal, Integer exitCode, String message, String errmsg) {
            super();
            this.exitSignal = exitSignal;
            this.exitCode = exitCode;
            this.message = message;
            this.errmsg = errmsg;
        }

        public String getExitSignal() {
            return exitSignal;
        }

        public Integer getExitCode() {
            return exitCode;
        }

        public String getMessage() {
            return message;
        }

        public String getErrmsg() {
            return errmsg;
        }

    }

    /**
     * {@link Ssh2KeyPair}
     * 
     * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
     * @version v1.0.0
     * @see
     */
    public final static class Ssh2KeyPair {

        /** Generate ssh2 privateKey. */
        final private String privateKey;

        /** Generate ssh2 publicKey. */
        final private String publicKey;

        public Ssh2KeyPair(String privateKey, String publicKey) {
            notNullOf(privateKey, "privateKey");
            notNullOf(publicKey, "publicKey");
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

    }

    /**
     * {@link AlgorithmType}
     * 
     * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
     * @version v1.0.0
     * @see
     */
    public static enum AlgorithmType {
        RSA, DSA, ECDSA
    }

    /**
     * Default environments path for different Linux distributions.</br>
     * e.g:
     * <p>
     * CentOS: /etc/bashrc </br>
     * Ubuntu: /etc/bash.bashrc
     * </p>
     */
    @Deprecated
    public static final String DEFAULT_LINUX_ENV_CMD = join(new String[] {
            // e.g: CentOS|Ubuntu
            "source /etc/profile",
            // e.g: CentOS
            "source /etc/bashrc",
            // e.g: Ubuntu
            "source /etc/bash.bashrc",
            // e.g: CentOS|Ubuntu
            "source ~/.profile",
            // e.g: CentOS|Ubuntu
            "source ~/.bashrc" }, " ");

}