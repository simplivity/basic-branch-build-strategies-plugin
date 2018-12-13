/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.branch.buildstrategies.basic;

import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.impl.mock.MockChangeRequestSCMHead;
import jenkins.scm.impl.mock.MockChangeRequestSCMRevision;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMHead;
import jenkins.scm.impl.mock.MockSCMRevision;
import jenkins.scm.impl.mock.MockSCMSource;
import jenkins.scm.impl.mock.MockTagSCMHead;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class ChangeRequestBuildStrategyImplTest {
    @Test
    public void given__regular_head__when__isApplicable__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockSCMHead("master");
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false).isApplicable(head),
                    is(false)
            );
        }
    }

    @Test
    public void given__tag_head__when__isApplicable__then__returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockSCMHead head = new MockTagSCMHead("master", System.currentTimeMillis());
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false).isApplicable(head),
                    is(false)
            );
        }
    }

    @Test
    public void given__cr_head__when__isAutomaticBuild__then__returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(SCMHeadOrigin.DEFAULT, 1, "master",
                    ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(false).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                            null
                    ),
                    is(true)
            );
        }
    }

    @Test
    public void given__cr_head_ignoring_target_changes__when__first_build__then__isAutomaticBuild_returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(SCMHeadOrigin.DEFAULT, 1, "master",
                    ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(true).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                            null
                    ),
                    is(true)
            );
        }
    }

    @Test
    public void given__cr_head_ignoring_target_changes__when__origin_change__then__isAutomaticBuild_returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(SCMHeadOrigin.DEFAULT, 1, "master",
                    ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(true).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "dummy"), "new-dummy"),
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy")
                    ),
                    is(true)
            );
        }
    }

    @Test
    public void given__cr_head_ignoring_target_changes__when__both_change__then__isAutomaticBuild_returns_true() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(SCMHeadOrigin.DEFAULT, 1, "master",
                    ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(true).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "dummy"), "new-dummy"),
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "old-dummy"), "dummy")
                    ),
                    is(true)
            );
        }
    }

    @Test
    public void given__cr_head_ignoring_target_changes__when__target_change__then__isAutomaticBuild_returns_false() throws Exception {
        try (MockSCMController c = MockSCMController.create()) {
            MockChangeRequestSCMHead head = new MockChangeRequestSCMHead(SCMHeadOrigin.DEFAULT, 1, "master",
                    ChangeRequestCheckoutStrategy.MERGE, true);
            assertThat(
                    new ChangeRequestBuildStrategyImpl(true).isAutomaticBuild(
                            new MockSCMSource(c, "dummy"),
                            head,
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "dummy"), "dummy"),
                            new MockChangeRequestSCMRevision(head,
                                    new MockSCMRevision(new MockSCMHead("master"), "old-dummy"), "dummy")
                    ),
                    is(false)
            );
        }
    }

}
