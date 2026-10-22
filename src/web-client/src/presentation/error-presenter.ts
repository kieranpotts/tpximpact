import { problemTypes, type Problem } from '../api/shortener-api.js'

/** The shorten form's fields, named as the API service names them. */
export type FormField = 'destinationUrl' | 'customAlias'

/**
 * What the shorten form shows when it can't be submitted: an error summary
 * at the top of the page, and a message against each field at fault. This is
 * the GOV.UK Design System's validation pattern.
 */
export interface FormErrors {
  summary: { text: string, href?: string }[]
  fields: Partial<Record<FormField, string>>
}

const formFields: readonly FormField[] = ['destinationUrl', 'customAlias']

function isFormField (field: string): field is FormField {
  return (formFields as readonly string[]).includes(field)
}

/** Turns the API service's rejection of the shorten form into errors to show. */
export function presentRejection (problem: Problem | undefined): FormErrors {
  if (problem?.type === problemTypes.aliasTaken) {
    return fieldErrors([
      { field: 'customAlias', message: 'That alias is already taken. Enter a different alias.' },
    ])
  }
  if (problem?.type === problemTypes.validationFailed) {
    const errors = (problem.errors ?? []).flatMap(({ field, message }) =>
      isFormField(field) ? [{ field, message }] : []
    )
    if (errors.length > 0) {
      return fieldErrors(errors)
    }
  }
  // The web client only sends well-formed requests, so any other rejection
  // isn't something the link editor can fix field by field.
  return {
    summary: [
      {
        text: 'Your short URL could not be created. Check what you entered, and try again.',
        href: '#destinationUrl',
      },
    ],
    fields: {},
  }
}

/** What the shorten form shows when the API service can't be reached. */
export function presentUnavailable (): FormErrors {
  return {
    summary: [{ text: 'The service is unavailable. Try again later.' }],
    fields: {},
  }
}

/** One message per field, the first the API service gave, in the form's order. */
function fieldErrors (errors: { field: FormField, message: string }[]): FormErrors {
  const fields: FormErrors['fields'] = {}
  for (const { field, message } of errors) {
    fields[field] ??= message
  }
  const summary = formFields.flatMap((field) => {
    const text = fields[field]
    return text === undefined ? [] : [{ text, href: `#${field}` }]
  })
  return { summary, fields }
}
