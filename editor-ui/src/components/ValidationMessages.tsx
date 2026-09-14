import type { FieldDiagnostic, ValidationResult } from "../types";
import { useEditor } from "../store/EditorContext";

export function fieldDiagnostics(diagnostics: FieldDiagnostic[], file: string, fields: string[], ruleId?: string) {
  return diagnostics.filter(diagnostic => diagnostic.file === file && fields.some(field => {
    if (diagnostic.field === field || (field.endsWith("]") && diagnostic.field.startsWith(`${field}.`))) return true;
    return Boolean(ruleId) && diagnostic.ruleId === ruleId
      && diagnostic.field === field.replace(/\[\d+\]/g, "");
  }));
}

function DiagnosticMessage({ diagnostic }: { diagnostic: FieldDiagnostic }) {
  return <span className={`field-diagnostic ${diagnostic.severity === "ERROR" ? "error" : "warning"}`}>
    <strong>{diagnostic.severity === "ERROR" ? "Error" : "Warning"}</strong>
    <span>{diagnostic.message}</span>
    <code>{diagnostic.field}{diagnostic.ruleId ? ` · ${diagnostic.ruleId}` : ""}</code>
  </span>;
}

export function FieldDiagnostics({ file, fields, ruleId }: { file: string; fields: string[]; ruleId?: string }) {
  const { validationResult } = useEditor();
  const messages = fieldDiagnostics(validationResult?.diagnostics ?? [], file, fields, ruleId);
  if (!messages.length) return null;
  return <span className="field-diagnostics" aria-live="polite">{messages.map((diagnostic, index) =>
    <DiagnosticMessage key={index} diagnostic={diagnostic}/>)}</span>;
}

export function ValidationMessages({ validation }: { validation: ValidationResult }) {
  const diagnostics = validation.diagnostics ?? [];
  return <div className="review-messages" aria-live="polite">
    {diagnostics.length ? <><h3>Fields to review</h3>{diagnostics.map((diagnostic, index) =>
      <article key={index}><div><code>{diagnostic.file}</code><DiagnosticMessage diagnostic={diagnostic}/></div></article>)}</> : null}
    {validation.errors.length ? <><h3>Validation errors</h3>{validation.errors.map((message, index) =>
      <article className="error" key={index}>{message}</article>)}</> : null}
    {validation.warnings.length ? <><h3>Validation warnings</h3>{validation.warnings.map((message, index) =>
      <article key={index}>{message}</article>)}</> : null}
  </div>;
}
