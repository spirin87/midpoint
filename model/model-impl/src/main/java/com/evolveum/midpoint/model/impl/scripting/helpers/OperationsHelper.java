/*
 * Copyright (c) 2010-2014 Evolveum
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

package com.evolveum.midpoint.model.impl.scripting.helpers;

import com.evolveum.midpoint.model.api.*;
import com.evolveum.midpoint.model.impl.scripting.ExecutionContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.statistics.StatisticsUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * @author mederly
 */
@Component
public class OperationsHelper {

    private static final Trace LOGGER = TraceManager.getTrace(OperationsHelper.class);

    @Autowired
    private ModelService modelService;

    @Autowired
    private ModelInteractionService modelInteractionService;

    @Autowired
    private PrismContext prismContext;

    public void applyDelta(ObjectDelta delta, ExecutionContext context, OperationResult result) throws ScriptExecutionException {
        applyDelta(delta, null, context, result);
    }

    public void applyDelta(ObjectDelta delta, ModelExecuteOptions options, ExecutionContext context, OperationResult result) throws ScriptExecutionException {
        try {
            modelService.executeChanges((Collection) Collections.singleton(delta), options, context.getTask(), result);
        } catch (ObjectAlreadyExistsException|ObjectNotFoundException|SchemaException|ExpressionEvaluationException|CommunicationException|ConfigurationException|PolicyViolationException|SecurityViolationException e) {
            throw new ScriptExecutionException("Couldn't modify object: " + e.getMessage(), e);
        }
    }

    public void applyDelta(ObjectDelta delta, ModelExecuteOptions options, boolean dryRun, ExecutionContext context, OperationResult result) throws ScriptExecutionException {
        try {
            if (dryRun) {
                modelInteractionService.previewChanges((Collection) Collections.singleton(delta), options, context.getTask(), result);
            } else {
                modelService.executeChanges((Collection) Collections.singleton(delta), options, context.getTask(), result);
            }
        } catch (ObjectAlreadyExistsException|ObjectNotFoundException|SchemaException|ExpressionEvaluationException|CommunicationException|ConfigurationException|PolicyViolationException|SecurityViolationException e) {
            throw new ScriptExecutionException("Couldn't modify object: " + e.getMessage(), e);
        }
    }

    public Collection<SelectorOptions<GetOperationOptions>> createGetOptions(boolean noFetch) {
        LOGGER.trace("noFetch = {}", noFetch);
        return noFetch ? SelectorOptions.createCollection(GetOperationOptions.createNoFetch()) : null;
    }

    public <T extends ObjectType> PrismObject<T> getObject(Class<T> type, String oid, boolean noFetch, ExecutionContext context, OperationResult result) throws ScriptExecutionException {
        try {
            return modelService.getObject(type, oid, createGetOptions(noFetch), context.getTask(), result);
        } catch (ConfigurationException|ObjectNotFoundException|SchemaException|CommunicationException|SecurityViolationException e) {
            throw new ScriptExecutionException("Couldn't get object: " + e.getMessage(), e);
        }
    }

    public ModelExecuteOptions createExecutionOptions(boolean raw) {
        ModelExecuteOptions options = new ModelExecuteOptions();
        options.setRaw(raw);
        return options;
    }

    public long recordStart(ExecutionContext context, ObjectType objectType) {
        long started = System.currentTimeMillis();
        if (context.getTask() != null && objectType != null) {
            context.getTask().recordIterativeOperationStart(PolyString.getOrig(objectType.getName()),
                    StatisticsUtil.getDisplayName(objectType.asPrismObject()),
                    StatisticsUtil.getObjectType(objectType, prismContext),
                    objectType.getOid());
        } else {
            LOGGER.warn("Couldn't record operation start in script execution; task = {}, objectType = {}",
                    context.getTask(), objectType);
        }
        return started;
    }

    public void recordEnd(ExecutionContext context, ObjectType objectType, long started, Throwable ex) {
        if (context.getTask() != null && objectType != null) {
            context.getTask().recordIterativeOperationEnd(
                    PolyString.getOrig(objectType.getName()),
                    StatisticsUtil.getDisplayName(objectType.asPrismObject()),
                    StatisticsUtil.getObjectType(objectType, prismContext),
                    objectType.getOid(),
                    started, ex);
        } else {
            LOGGER.warn("Couldn't record operation end in script execution; task = {}, objectType = {}",
                    context.getTask(), objectType);
        }
        if (context.getTask() != null) {
            context.getTask().setProgress(context.getTask().getProgress() + 1);
        }
    }
}
