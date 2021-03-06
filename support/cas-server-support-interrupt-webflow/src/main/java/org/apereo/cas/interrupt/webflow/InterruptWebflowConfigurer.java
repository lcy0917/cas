package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link InterruptWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InterruptWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String VIEW_ID_INTERRUPT_VIEW = "interruptView";
    private static final String INTERRUPT_VIEW_ID = "casInterruptView";
    private static final String STATE_ID_INQUIRE_INTERRUPT_ACTION = "inquireInterruptAction";
    private static final String STATE_ID_FINALIZE_INTERRUPT_ACTION = "finalizeInterruptFlowAction";
    private static final String STATE_ID_PREPARE_INTERRUPT_VIEW_ACTION = "prepareInterruptViewAction";

    public InterruptWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry flowDefinitionRegistry) {
        super(flowBuilderServices, flowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        if (flow != null) {
            createInquireActionState(flow);
            createInterruptView(flow);
            createTransitionStateToInterrupt(flow);
        }
    }

    private void createTransitionStateToInterrupt(final Flow flow) {
        final ActionState submit = getRealSubmissionState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_INQUIRE_INTERRUPT_ACTION, true);
    }

    private ActionState getRealSubmissionState(final Flow flow) {
        return (ActionState) flow.getState(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
    }

    private void createInterruptView(final Flow flow) {
        final ViewState viewState = createViewState(flow, VIEW_ID_INTERRUPT_VIEW, INTERRUPT_VIEW_ID);
        viewState.getEntryActionList().add(createEvaluateAction(STATE_ID_PREPARE_INTERRUPT_VIEW_ACTION));
        createStateDefaultTransition(viewState, STATE_ID_FINALIZE_INTERRUPT_ACTION);

        final String target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        final ActionState finalizeInterrupt = createActionState(flow, STATE_ID_FINALIZE_INTERRUPT_ACTION, 
                createEvaluateAction(STATE_ID_FINALIZE_INTERRUPT_ACTION));
        createTransitionForState(finalizeInterrupt, CasWebflowConstants.TRANSITION_ID_SUCCESS, target);
        createTransitionForState(finalizeInterrupt, CasWebflowConstants.TRANSITION_ID_NO, "finishedInterrupt");
        createEndState(flow, "finishedInterrupt");
    }

    private void createInquireActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, STATE_ID_INQUIRE_INTERRUPT_ACTION, createEvaluateAction(STATE_ID_INQUIRE_INTERRUPT_ACTION));

        final String target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        final Transition noInterruptTransition = createTransition(CasWebflowConstants.TRANSITION_ID_NO, target);
        actionState.getTransitionSet().add(noInterruptTransition);

        final Transition yesInterruptTransition = createTransition(CasWebflowConstants.TRANSITION_ID_YES, VIEW_ID_INTERRUPT_VIEW);
        actionState.getTransitionSet().add(yesInterruptTransition);
    }
}
