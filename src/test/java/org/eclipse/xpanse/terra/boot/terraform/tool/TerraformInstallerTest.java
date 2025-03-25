package org.eclipse.xpanse.terra.boot.terraform.tool;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.annotation.Resource;
import java.io.File;
import java.util.Set;
import org.eclipse.xpanse.terra.boot.models.exceptions.InvalidTerraformToolException;
import org.eclipse.xpanse.terra.boot.terraform.utils.SystemCmd;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = {
            TerraformInstaller.class,
            TerraformVersionsHelper.class,
            TerraformVersionsCache.class,
            TerraformVersionsFetcher.class,
            SystemCmd.class
        },
        properties = {
            "support.default.terraform.versions.only=true",
            "terraform.versions.github.api.url=http://localhost:8089"
        })
class TerraformInstallerTest {

    private static WireMockServer wireMockServer;

    @Value("${terraform.default.supported.versions:1.6.0,1.7.0,1.8.0,1.9.0,1.10.0}")
    private String terraformVersions;

    @Resource private TerraformInstaller installer;
    @Resource private TerraformVersionsHelper versionHelper;
    @Resource private TerraformVersionsCache versionsCache;

    @Disabled
    @Test
    void testGetExecutableTerraformByVersion() {
        Set<String> defaultVersions = Set.of(terraformVersions.split(","));
        Set<String> cachedVersions = versionsCache.getAvailableVersions();
        assertTrue(cachedVersions.containsAll(defaultVersions));
        assertTrue(cachedVersions.size() >= defaultVersions.size());

        String requiredVersion = "";
        String terraformPath = installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion);
        assertEquals("terraform", terraformPath);

        String requiredVersion1 = "= 1.7.0";
        String[] operatorAndNumber1 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion1);
        String terraformPath1 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion1);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath1), operatorAndNumber1[0], operatorAndNumber1[1]));

        String requiredVersion2 = "<= v1.6.0";
        String[] operatorAndNumber2 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion2);
        String terraformPath2 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion2);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath2), operatorAndNumber2[0], operatorAndNumber2[1]));

        String requiredVersion3 = ">= v1.9.0";
        String[] operatorAndNumber3 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion3);
        String terraformPath3 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion3);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath3), operatorAndNumber3[0], operatorAndNumber3[1]));

        String requiredVersion4 = ">= 100.0.0";
        assertThrows(
                InvalidTerraformToolException.class,
                () -> installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion4));
    }

    @BeforeAll
    static void setupWireMockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
        wireMockServer.start();

        wireMockServer.stubFor(
                get(urlPathEqualTo("/repos/hashicorp/terraform/tags"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                [
                                                    {"name": "v1.7.0"},
                                                    {"name": "v1.8.0"},
                                                    {"name": "v1.9.0"},
                                                    {"name": "v1.10.0"}
                                                ]
                                                """)));
    }

    @AfterAll
    static void teardownWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void testGetExecutableTerraformByVersionWithWiremock() {
        Set<String> defaultVersions = Set.of(terraformVersions.split(","));
        Set<String> cachedVersions = versionsCache.getAvailableVersions();
        assertTrue(cachedVersions.containsAll(defaultVersions));
        assertTrue(cachedVersions.size() >= defaultVersions.size());

        String requiredVersion = "";
        String terraformPath = installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion);
        assertEquals("terraform", terraformPath);

        String requiredVersion1 = "= 1.7.0";
        String[] operatorAndNumber1 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion1);
        String terraformPath1 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion1);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath1), operatorAndNumber1[0], operatorAndNumber1[1]));

        String requiredVersion2 = "<= v1.6.0";
        String[] operatorAndNumber2 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion2);
        String terraformPath2 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion2);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath2), operatorAndNumber2[0], operatorAndNumber2[1]));

        String requiredVersion3 = ">= v1.9.0";
        String[] operatorAndNumber3 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion3);
        String terraformPath3 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion3);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath3), operatorAndNumber3[0], operatorAndNumber3[1]));

        String requiredVersion4 = ">= 100.0.0";
        assertThrows(
                InvalidTerraformToolException.class,
                () -> installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion4));
    }
}
