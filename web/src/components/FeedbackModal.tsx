import React from 'react';
import './FeedbackModal.css';

type FeedbackVariant = 'success' | 'error' | 'info';

interface FeedbackModalProps {
    open: boolean;
    title: string;
    message: string;
    variant?: FeedbackVariant;
    buttonLabel?: string;
    onClose: () => void;
}

const variantEmoji: Record<FeedbackVariant, string> = {
    success: '✅',
    error: '⚠️',
    info: 'ℹ️'
};

export default function FeedbackModal({
    open,
    title,
    message,
    variant = 'info',
    buttonLabel = 'Entendi',
    onClose
}: FeedbackModalProps) {
    if (!open) return null;

    return (
        <div className="feedback-overlay" role="dialog" aria-modal="true" aria-label={title}>
            <div className={`feedback-card feedback-${variant}`}>
                <div className="feedback-emoji" aria-hidden="true">{variantEmoji[variant]}</div>
                <h2>{title}</h2>
                <p>{message}</p>
                <button type="button" className="feedback-button" onClick={onClose}>
                    {buttonLabel}
                </button>
            </div>
        </div>
    );
}
