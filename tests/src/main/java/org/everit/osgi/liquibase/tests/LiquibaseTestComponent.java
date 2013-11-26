package org.everit.osgi.liquibase.tests;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.liquibase.component.LiquibaseService;
import org.junit.Test;
import org.osgi.framework.BundleContext;

@Component(name = "LiquibaseTest", immediate = true)
@Service(value = LiquibaseTestComponent.class)
@Properties({ @Property(name = "osgitest.testEngine", value = "junit4"),
        @Property(name = "osgitest.testId", value = "liquibaseTest") })
public class LiquibaseTestComponent {

    @Reference
    private ConfigurationInitComponent configInit;

    @Reference
    private LiquibaseService liquibaseService;

    @Reference
    private DataSource dataSource;

    private BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Test
    public void testSimpleDatabaseCreation() {

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            liquibaseService.process(connection, bundleContext, "META-INF/liquibase/changelog.xml");

            dropAllTables(connection);

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void dropAllTables(Connection connection) throws LiquibaseException {
        Database database =
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        database.setDefaultCatalogName("TEST");
        database.setDefaultSchemaName("TEST");
        Liquibase liquibase = new Liquibase(null, null, database);
        liquibase.dropAll();
    }

    @Test
    @TestDuringDevelopment
    public void testDoubleDatabaseCreation() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            liquibaseService.process(connection, bundleContext, "META-INF/liquibase/changelog.xml");

            liquibaseService.process(connection, bundleContext, "META-INF/liquibase/changelog.xml");

            dropAllTables(connection);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
