/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.xa.manager;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.jta.UserTransactionManager;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.XADataSource;
import javax.transaction.SystemException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class AtomikosTransactionManagerTest {
    
    private AtomikosTransactionManager atomikosTransactionManager = new AtomikosTransactionManager();
    
    @Mock
    private UserTransactionManager userTransactionManager;
    
    @Mock
    private UserTransactionService userTransactionService;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Before
    @SneakyThrows
    public void setUp() {
        ReflectiveUtil.setProperty(atomikosTransactionManager, "underlyingTransactionManager", userTransactionManager);
        ReflectiveUtil.setProperty(atomikosTransactionManager, "userTransactionService", userTransactionService);
    }
    
    @Test
    public void assertStartup() {
        atomikosTransactionManager.startup();
        verify(userTransactionService).init();
    }
    
    @Test
    public void assertShutdown() {
        atomikosTransactionManager.destroy();
        verify(userTransactionService).shutdown(true);
    }
    
    @Test
    public void assertBeginWithoutException() throws Exception {
        atomikosTransactionManager.begin();
        verify(userTransactionManager).begin();
    }
    
    @Test(expected = ShardingException.class)
    public void assertBeginWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).begin();
        atomikosTransactionManager.begin();
    }
    
    @Test
    public void assertCommitWithoutException() throws Exception {
        atomikosTransactionManager.commit();
        verify(userTransactionManager).commit();
    }
    
    @Test(expected = ShardingException.class)
    public void assertCommitWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).commit();
        atomikosTransactionManager.commit();
    }
    
    @Test
    public void assertRollbackWithoutException() throws Exception {
        atomikosTransactionManager.rollback();
        verify(userTransactionManager).rollback();
    }
    
    @Test(expected = ShardingException.class)
    public void assertRollbackWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).rollback();
        atomikosTransactionManager.rollback();
    }
    
    @Test
    public void assertRegisterRecoveryResource() {
        atomikosTransactionManager.registerRecoveryResource("ds1", xaDataSource);
        verify(userTransactionService).registerResource(any(AtomikosXARecoverableResource.class));
    }
    
    @Test
    public void assertRemoveRecoveryResource() {
        atomikosTransactionManager.removeRecoveryResource("ds1", xaDataSource);
        verify(userTransactionService).removeResource(any(AtomikosXARecoverableResource.class));
    }
}
