/*
 * Copyright (c) 2024 Ruxy
 * Released under the MIT license
 * https://opensource.org/license/mit
 */

package net.rk4z.beacon

/**
 * Annotation for marking a class as an event handler.
 * Classes annotated with [IEventHandler] are recognized by the event dispatch system
 * and can contain methods that handle events.
 *
 * The annotation focuses on classes and is retained at runtime, allowing for reflection-based
 * processing of event handlers.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler

